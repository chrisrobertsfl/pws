package com.kohls.base

import org.instancio.Gen.oneOf

object Generators {

     val pathNames = listOf(
        "documents",
        "photos",
        "music",
        "videos",
        "downloads",
        "projects",
        "archive",
        "backup",
        "temp",
        "logs",
        "invoices",
        "receipts",
        "reports",
        "presentations",
        "spreadsheets",
        "letters",
        "memos",
        "notes",
        "diaries",
        "manuscripts",
        "scripts",
        "theses",
        "journals",
        "articles",
        "essays",
        "portfolios",
        "sketches",
        "drafts",
        "designs",
        "blueprints",
        "databases",
        "data",
        "analysis",
        "research",
        "statistics",
        "surveys",
        "studies",
        "experiments",
        "findings",
        "conclusions",
        "proposals",
        "plans",
        "strategies",
        "roadmaps",
        "budgets",
        "agendas",
        "minutes",
        "schedules",
        "timetables",
        "calendars",
        "tasks",
        "checklists",
        "instructions",
        "guides",
        "manuals",
        "tutorials",
        "courses",
        "lectures",
        "workshops",
        "seminars",
        "webinars",
        "podcasts",
        "recordings",
        "interviews",
        "speeches",
        "biographies",
        "autobiographies",
        "narratives",
        "stories",
        "novels",
        "poems",
        "plays",
        "screenplays",
        "scripts",
        "dialogues",
        "profiles",
        "biographies",
        "summaries",
        "overviews",
        "synopses",
        "reviews",
        "critiques",
        "feedback",
        "comments",
        "opinions",
        "blogs",
        "articles",
        "posts",
        "updates",
        "announcements",
        "notifications",
        "alerts",
        "messages",
        "correspondence",
        "communications",
        "emails",
        "letters",
        "memos",
        "reports",
        "bulletins"
    )

     val fileExtensions = listOf(
        ".txt", ".pdf", ".docx", ".xlsx", ".pptx", ".jpg", ".png", ".mp3", ".mp4", ".zip"
    )


    inline fun <reified T> pick(list: List<T>, howManyTimes: Int = 1): List<T> = buildList {
        repeat(howManyTimes) {
            add(oneOf(*list.toTypedArray()).get())
        }
    }

     inline fun <reified T> pickOne(list: List<T>): T = pick(list).first()

}