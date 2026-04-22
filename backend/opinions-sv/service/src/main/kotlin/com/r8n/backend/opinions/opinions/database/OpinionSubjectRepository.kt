package com.r8n.backend.opinions.opinions.database

import com.r8n.backend.opinions.opinions.persistence.OpinionSubjectPersistence
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OpinionSubjectRepository : JpaRepository<OpinionSubjectPersistence, UUID>