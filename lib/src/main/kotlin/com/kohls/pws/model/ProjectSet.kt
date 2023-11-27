package com.kohls.pws.model

import com.kohls.pws.Parameters

data class ProjectSet(val name: ProjectSetName, val projects: Set<Project>, val parameters : Parameters)