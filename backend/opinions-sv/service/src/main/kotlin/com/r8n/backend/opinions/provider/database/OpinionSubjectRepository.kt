package com.r8n.backend.opinions.provider.database

import com.r8n.backend.opinions.persistence.OpinionSubjectPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionSubjectRepository : JpaRepository<OpinionSubjectPersistence, UUID>
