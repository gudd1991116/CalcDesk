package com.qiyuan.calcdesk.data

/** “时间间隔能算什么” content from WebToolPage. */
object TimeIntervalHelp {
    val titleZh = "时间间隔能算什么？"
    val titleEn = "What can a time interval calculator do?"

    val introZh = "核心公式：结束时间 − 开始时间 = 中间间隔时间。适合日常计时与安排。"
    val introEn = "Core idea: end time − start time = elapsed interval. Handy for everyday timing and planning."

    val tipZh = "若已知开始时间与要加的时长（如煲汤 4.5h），请用「日期推算」。"
    val tipEn = "If you know a start time plus a duration (e.g. soup for 4.5h), use Date offset instead."

    data class Scene(val titleZh: String, val titleEn: String, val bodyZh: String, val bodyEn: String) {
        fun title(zh: Boolean) = if (zh) titleZh else titleEn
        fun body(zh: Boolean) = if (zh) bodyZh else bodyEn
    }

    val scenes: List<Scene> = listOf(
        Scene("预约饭 / 电饭锅", "Meal planning / rice cooker", "18:00 − 8:00 = 10h，从早上到开饭要等多久。", "18:00 − 8:00 = 10h until dinner is ready."),
        Scene("加班时长", "Overtime hours", "下班时间 − 上班时间 = 加班/在岗时长。", "Clock-out − clock-in = hours worked / overtime."),
        Scene("写作业 / 自习", "Homework / study", "结束学习 − 开始学习 = 作业或自习时长。", "Finish study − start study = study duration."),
        Scene("驾车时间", "Driving time", "到达时间 − 出发时间 = 路上用时。", "Arrival − departure = time on the road."),
        Scene("停车时间", "Parking duration", "取车时间 − 停车时间 = 停车多久。", "Leave lot − park time = how long you parked."),
        Scene("换乘时间", "Transit transfer wait", "下一班出发 − 到站时间 = 换乘等待。", "Next departure − arrival = transfer wait."),
        Scene("运动时长", "Workout length", "结束运动 − 开始运动 = 锻炼时间。", "End workout − start workout = exercise time."),
        Scene("睡眠时长", "Sleep duration", "起床时间 − 入睡时间 = 睡了多久。", "Wake time − bedtime = how long you slept."),
        Scene("服药间隔", "Medicine interval", "下次服药 − 上次服药 = 间隔是否够。", "Next dose − last dose = gap between doses."),
    )
}
