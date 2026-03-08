package com.r8n.backend.opinions.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService {
    fun getSubjectName(id: UUID) = "Subject"
}