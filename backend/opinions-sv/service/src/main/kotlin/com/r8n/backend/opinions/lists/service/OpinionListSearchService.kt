package com.r8n.backend.opinions.lists.service

import com.r8n.backend.opinions.access.database.AccessRequestRepository
import com.r8n.backend.opinions.access.domain.RequestStatusEnum
import com.r8n.backend.opinions.access.service.AccessService
import com.r8n.backend.opinions.lists.database.OpinionListRepository
import com.r8n.backend.opinions.lists.database.OpinionsToOpinionListsRepository
import com.r8n.backend.opinions.lists.domain.LocationFilter
import com.r8n.backend.opinions.lists.domain.OpinionList
import com.r8n.backend.opinions.lists.domain.OpinionListInfo
import com.r8n.backend.opinions.lists.domain.OpinionListPrivacyEnum
import com.r8n.backend.opinions.lists.domain.OpinionListSearchFilters
import com.r8n.backend.opinions.lists.persistence.OpinionListPersistence
import com.r8n.backend.opinions.opinions.database.OpinionRepository
import com.r8n.backend.opinions.opinions.database.OpinionSubjectRepository
import com.r8n.backend.opinions.opinions.database.ReferentRepository
import com.r8n.backend.users.integration.api.UsersInternalApi
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OpinionListSearchService(
    private val opinionListRepository: OpinionListRepository,
    private val opinionListService: OpinionListService,
    private val opinionsAssignmentRepository: OpinionsToOpinionListsRepository,
    private val accessService: AccessService,
    private val accessRequestRepository: AccessRequestRepository,
    private val usersApi: UsersInternalApi,
    private val referentRepository: ReferentRepository,
    private val subjectRepository: OpinionSubjectRepository,
    private val opinionRepository: OpinionRepository,
) {
    fun getMine(
        ownerId: UUID,
        pageable: Pageable,
        includingVirtual: Boolean = true,
    ): Page<OpinionListInfo> {
        val lists = opinionListRepository.findByOwner(ownerId, pageable)
        val infoList =
            lists.content
                .map { list ->
                    OpinionListInfo(
                        id = list.id!!,
                        name = list.name,
                        owner = list.owner,
                        privacy = list.privacy,
                        opinionsCount = opinionListService.countVisibleOpinions(list.id!!, ownerId),
                        grantedAccessCount = accessService.countAcceptedForList(list.id!!),
                    )
                }.toMutableList()

        if (includingVirtual && pageable.pageNumber == 0) {
            val allMyOpinions =
                OpinionListInfo(
                    id = null,
                    name = "[ALL]",
                    owner = ownerId,
                    privacy = OpinionListPrivacyEnum.PRIVATE,
                    opinionsCount = opinionRepository.countByOwner(ownerId),
                    grantedAccessCount = 0,
                )
            infoList.add(0, allMyOpinions)
        }

        return PageImpl(
            infoList,
            pageable,
            lists.totalElements +
                (if (includingVirtual && (pageable.pageNumber == 0 || lists.totalElements > 0)) 1 else 0),
        )
    }

    fun getListsFull(
        ownerId: UUID,
        pageable: Pageable,
        includingVirtual: Boolean = true,
    ): Page<OpinionList> {
        val lists = opinionListRepository.findByOwner(ownerId, pageable)
        val fullLists = lists.content.map { opinionListService.getList(it.id!!, ownerId) }.toMutableList()

        if (includingVirtual && pageable.pageNumber == 0) {
            val allMyOpinions = opinionListService.getMyVirtualList(ownerId)
            fullLists.add(0, allMyOpinions)
        }

        return PageImpl(
            fullLists,
            pageable,
            lists.totalElements +
                (if (includingVirtual && (pageable.pageNumber == 0 || lists.totalElements > 0)) 1 else 0),
        )
    }

    fun getApprovedListsWithNamesAndOwners(
        requesterId: UUID,
        pageable: Pageable,
    ): Page<OpinionListInfo> {
        val approvedRequests =
            accessRequestRepository.findAllByFilters(
                listId = null,
                requesterId = requesterId,
                ownerId = null,
                status = RequestStatusEnum.ACCEPTED,
                pageable = pageable,
            )

        val listIds = approvedRequests.map { it.list }.content
        val lists = opinionListRepository.findAllById(listIds).associateBy { it.id }

        val infoList =
            approvedRequests.content.mapNotNull { request ->
                val list = lists[request.list]
                if (list != null && list.privacy == OpinionListPrivacyEnum.SEARCHABLE) {
                    OpinionListInfo(
                        id = list.id!!,
                        name = list.name,
                        owner = list.owner,
                        privacy = list.privacy,
                        opinionsCount = opinionListService.countVisibleOpinions(list.id!!, requesterId),
                        grantedAccessCount = accessService.countAcceptedForList(list.id!!),
                    )
                } else {
                    null
                }
            }
        return PageImpl(infoList, pageable, approvedRequests.totalElements)
    }

    @Transactional(readOnly = true)
    fun search(
        requesterId: UUID,
        filters: OpinionListSearchFilters,
        pageable: Pageable,
    ): Page<OpinionListInfo> {
        if (!hasFilters(filters)) {
            return Page.empty(pageable)
        }

        val resultIds = performFiltering(requesterId, filters)

        if (resultIds.isEmpty()) {
            return Page.empty(pageable)
        }

        return applySortingAndPagination(resultIds, pageable)
    }

    private fun performFiltering(
        requesterId: UUID,
        filters: OpinionListSearchFilters,
    ): Set<UUID> {
        val authorIdsFromName = getAuthorIdsFromName(filters.authorNameSubstring)

        // Base results from primary filters (name and author)
        val resultIds =
            opinionListRepository
                .searchIds(
                    nameSubstring = filters.nameSubstring?.trim()?.takeIf { it.isNotBlank() },
                    authorId = filters.authorId,
                    authorIds = authorIdsFromName,
                    requesterId = requesterId,
                    searchablePrivacy = OpinionListPrivacyEnum.SEARCHABLE,
                ).toMutableSet()

        if (resultIds.isEmpty()) return emptySet()

        // Apply additional filters as intersections
        filterByLocationSubstring(resultIds, filters.locationFilter?.containsLocationSubstring)
        if (resultIds.isEmpty()) return emptySet()

        filterBySubjectSubstring(resultIds, filters.containsSubjectSubstring)
        if (resultIds.isEmpty()) return emptySet()

        filterByLocationRadius(resultIds, filters.locationFilter)
        if (resultIds.isEmpty()) return emptySet()

        filterByYoungerThan(resultIds, filters.someOpinionsYoungerThan)
        if (resultIds.isEmpty()) return emptySet()

        filterByTextInAny(resultIds, filters.findThisTextInAnyOfTheAbove, requesterId)

        return resultIds
    }

    private fun applySortingAndPagination(
        resultIds: Set<UUID>,
        pageable: Pageable,
    ): Page<OpinionListInfo> {
        // Fetch all matching items to allow correct sorting and pagination in-memory
        // since some fields (like opinionsCount) are not available for JPA-level sorting
        val isNativeSort = pageable.sort.isSorted && pageable.sort.all { it.property in listOf("name", "listName") }

        if (isNativeSort) {
            // Map listName to name for DB sorting if needed
            val orders =
                pageable.sort
                    .map { order ->
                        val property = if (order.property == "listName") "name" else order.property
                        Sort.Order(order.direction, property)
                    }.toList()
            val dbSort = Sort.by(orders)
            val dbPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, dbSort)

            val page = opinionListRepository.findAllByIdIn(resultIds, dbPageable)
            return page.map { mapToOpinionListInfo(it) }
        }

        // Fallback to in-memory sorting for non-native fields
        val allMatchingPersistence = opinionListRepository.findAllById(resultIds)

        val ownerNamesMap =
            if (pageable.sort.any { it.property == "ownerName" }) {
                val ownerIds = allMatchingPersistence.map { it.owner }.toSet()
                ownerIds.associateWith { usersApi.getUserName(it) }
            } else {
                emptyMap()
            }

        val opinionsCountMap =
            if (pageable.sort.any { it.property == "opinionsCount" }) {
                allMatchingPersistence.associate { it.id!! to opinionsAssignmentRepository.countByOpinionList(it.id!!) }
            } else {
                emptyMap()
            }

        val sortedPersistence =
            if (pageable.sort.isSorted) {
                var comparator: Comparator<OpinionListPersistence>? = null
                for (order in pageable.sort) {
                    val propComparator: Comparator<OpinionListPersistence>? =
                        when (order.property) {
                            "listName", "name" -> compareBy { it.name.lowercase() }
                            "ownerName" -> compareBy { ownerNamesMap[it.owner]?.lowercase() ?: "" }
                            "opinionsCount" -> compareBy { opinionsCountMap[it.id!!] ?: 0L }
                            else -> null
                        }
                    if (propComparator != null) {
                        val directedComparator =
                            if (order.direction.isDescending) {
                                propComparator.reversed()
                            } else {
                                propComparator
                            }
                        comparator =
                            if (comparator == null) directedComparator else comparator.thenComparing(directedComparator)
                    }
                }
                if (comparator != null) allMatchingPersistence.sortedWith(comparator) else allMatchingPersistence
            } else {
                allMatchingPersistence
            }

        val total = sortedPersistence.size.toLong()
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(sortedPersistence.size)

        val contentPersistence =
            if (start < sortedPersistence.size) {
                sortedPersistence.subList(start, end)
            } else {
                emptyList()
            }

        return PageImpl(
            contentPersistence.map {
                mapToOpinionListInfo(
                    list = it,
                    opinionsCount = opinionsCountMap[it.id!!],
                    ownerName = ownerNamesMap[it.owner],
                )
            },
            pageable,
            total,
        )
    }

    private fun hasFilters(filters: OpinionListSearchFilters): Boolean =
        !filters.nameSubstring.isNullOrBlank() ||
            filters.authorId != null ||
            !filters.authorNameSubstring.isNullOrBlank() ||
            !filters.locationFilter?.containsLocationSubstring.isNullOrBlank() ||
            filters.someOpinionsYoungerThan != null ||
            !filters.containsSubjectSubstring.isNullOrBlank() ||
            !filters.findThisTextInAnyOfTheAbove.isNullOrBlank() ||
            (
                filters.locationFilter?.latitude != null &&
                    filters.locationFilter.longitude != null &&
                    filters.locationFilter.radiusInMeters != null
            )

    private fun getAuthorIdsFromName(authorNameSubstring: String?): Set<UUID>? =
        if (!authorNameSubstring.isNullOrBlank()) {
            usersApi
                .findUsersByNameSubstring(authorNameSubstring)
                .map { it.id!! }
                .toSet()
                .ifEmpty { null }
        } else {
            null
        }

    private fun filterByLocationSubstring(
        resultIds: MutableSet<UUID>,
        loc: String?,
    ) {
        loc?.trim()?.takeIf { it.isNotBlank() }?.let { substring ->
            val referentIds = referentRepository.findIdsByAddressContainingIgnoreCase(substring)
            resultIds.retainAll(findListIdsByReferents(referentIds))
        }
    }

    private fun filterBySubjectSubstring(
        resultIds: MutableSet<UUID>,
        sub: String?,
    ) {
        sub?.trim()?.takeIf { it.isNotBlank() }?.let { substring ->
            val subjectIds = subjectRepository.findIdsByNameContainingIgnoreCase(substring)
            resultIds.retainAll(findListIdsBySubjects(subjectIds))
        }
    }

    private fun filterByLocationRadius(
        resultIds: MutableSet<UUID>,
        filter: LocationFilter?,
    ) {
        val lat = filter?.latitude
        val lng = filter?.longitude
        val radius = filter?.radiusInMeters
        if (lat != null && lng != null && radius != null) {
            val referentIds = referentRepository.findIdsByLocationRadius(lat, lng, radius)
            resultIds.retainAll(findListIdsByReferents(referentIds))
        }
    }

    private fun filterByYoungerThan(
        resultIds: MutableSet<UUID>,
        youngerThan: Instant?,
    ) {
        youngerThan?.let { timestamp ->
            val opinionIds = opinionRepository.findIdsByTimestampAfter(timestamp)
            resultIds.retainAll(findListIdsByOpinions(opinionIds))
        }
    }

    private fun filterByTextInAny(
        resultIds: MutableSet<UUID>,
        text: String?,
        requesterId: UUID,
    ) {
        text?.trim()?.takeIf { it.isNotBlank() }?.let { substring ->
            val matchedListIds = mutableSetOf<UUID>()

            // 1. Name match
            matchedListIds.addAll(
                opinionListRepository.searchIds(
                    nameSubstring = substring,
                    authorId = null,
                    authorIds = null,
                    requesterId = requesterId,
                    searchablePrivacy = OpinionListPrivacyEnum.SEARCHABLE,
                ),
            )

            // 2. Subject name match
            val subjectIds = subjectRepository.findIdsByNameContainingIgnoreCase(substring)
            matchedListIds.addAll(findListIdsBySubjects(subjectIds))

            // 3. Location match
            val referentIds = referentRepository.findIdsByAddressContainingIgnoreCase(substring)
            matchedListIds.addAll(findListIdsByReferents(referentIds))

            resultIds.retainAll(matchedListIds)
        }
    }

    private fun findListIdsByReferents(referentIds: Set<UUID>): Set<UUID> =
        if (referentIds.isNotEmpty()) {
            findListIdsBySubjects(subjectRepository.findIdsByReferentIn(referentIds))
        } else {
            emptySet()
        }

    private fun findListIdsBySubjects(subjectIds: Set<UUID>): Set<UUID> =
        if (subjectIds.isNotEmpty()) {
            findListIdsByOpinions(opinionRepository.findIdsBySubjectIn(subjectIds))
        } else {
            emptySet()
        }

    private fun findListIdsByOpinions(opinionIds: Set<UUID>): Set<UUID> =
        if (opinionIds.isNotEmpty()) {
            opinionsAssignmentRepository.findOpinionListIdsByOpinionIn(opinionIds)
        } else {
            emptySet()
        }

    private fun mapToOpinionListInfo(
        list: OpinionListPersistence,
        opinionsCount: Long? = null,
        ownerName: String? = null,
    ): OpinionListInfo =
        OpinionListInfo(
            id = list.id!!,
            name = list.name,
            owner = list.owner,
            privacy = list.privacy,
            opinionsCount = opinionsCount ?: opinionsAssignmentRepository.countByOpinionList(list.id!!),
            grantedAccessCount = accessService.countAcceptedForList(list.id!!),
            ownerName = ownerName,
        )
}
