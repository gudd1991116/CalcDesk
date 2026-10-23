package com.qiyuan.calcdesk.data

/** Extra articles / FAQs for the timestamp tool help (from WebToolPage). */
data class HelpArticle(
    val id: String,
    val titleEn: String,
    val titleZh: String,
    val paragraphsEn: List<String>,
    val paragraphsZh: List<String>,
) {
    fun title(zh: Boolean) = if (zh) titleZh else titleEn
    fun paragraphs(zh: Boolean) = if (zh) paragraphsZh else paragraphsEn
}

object TimestampHelp {

    /** Shown inline in the main help dialog (简介 + 由来). */
    val aboutSummaryEn =
        "A Unix timestamp is the number of seconds (excluding leap seconds) since January 1, 1970 00:00:00 UTC — also called the Unix epoch. It is not human-friendly, so this converter turns timestamps into readable dates and back."
    val aboutSummaryZh =
        "Unix 时间戳是从 1970 年 1 月 1 日 00:00:00（UTC）起至某一时刻所经过的秒数（不含闰秒），也常称为 Unix epoch。这种格式不便于人阅读，因此本工具可在时间戳与可读日期时间之间双向转换。"

    val originTitleEn = "Where did Unix timestamps come from?"
    val originTitleZh = "时间戳的由来"
    val originBodyEn =
        "In the early 1970s, Unix systems needed a simple, universal way to store moments in time. Engineers chose midnight on 1 January 1970 (UTC) as the “epoch” — time zero. A Unix timestamp counts how many seconds have passed since that instant (leap seconds are ignored in practice). Because it is always UTC-based, the same number means the same moment everywhere, which is why servers, APIs, and databases rely on it."
    val originBodyZh =
        "20 世纪 70 年代初，Unix 系统需要一种简单、统一的方式存储时间点。工程师将 1970 年 1 月 1 日 00:00:00（UTC）定为「纪元」（epoch）起点，Unix 时间戳即从此刻起累计的秒数（实际使用中忽略闰秒）。由于始终以 UTC 为基准，同一数字在全球表示同一瞬间，因此服务器、API 和数据库广泛采用。"

    val howToTitleEn = "How to use this screen"
    val howToTitleZh = "本页怎么用"
    val howToBodyEn =
        "The app auto-detects seconds (about 10 digits) vs milliseconds (about 13 digits). Edit either field and the other updates live. Use Follow now to tick with the current time, clear/paste on each field, and pick a date-time for the local field. The result card lists unit, seconds, millis, ISO, zone, and weekday — tap Copy to copy everything."
    val howToBodyZh =
        "本页会按位数自动识别秒级（约 10 位）与毫秒级（约 13 位）时间戳。改任一输入框，另一框实时联动。可用「现在」跟随当前时间，输入框可清除/粘贴，本地时间可点日历选择。结果区含输入单位、秒、毫秒、ISO、时区与星期，可一键复制。"

    /** Remaining topics shown as links → detail dialogs. */
    val linkArticles: List<HelpArticle> = listOf(
        HelpArticle(
            id = "dst",
            titleEn = "What is daylight saving time (DST)?",
            titleZh = "什么是夏令时（DST）？",
            paragraphsEn = listOf(
                "Daylight saving time shifts clocks forward by one hour in spring and back in autumn. The idea appeared in the early 20th century (widely credited to proposals around 1907) as a way to make better use of evening daylight and save energy on lighting.",
                "Not every country uses DST, and rules differ by region. When DST starts or ends, local wall-clock times can repeat or skip an hour — that is why converting timestamps must use a time zone database (IANA), not a fixed UTC offset alone.",
                "Examples: the United States and most of Europe observe DST; Japan, India, China, Vietnam, and the Philippines do not.",
            ),
            paragraphsZh = listOf(
                "夏令时在春季将时钟拨快 1 小时、秋季拨回，最早可追溯到 20 世纪初（约 1907 年前后的相关提案），目的是延长傍晚日照、节约照明能源。",
                "并非所有国家都实行夏令时，规则也因地区而异。夏令时开始或结束时，本地钟面时间可能出现重复或跳过的 1 小时——因此转换时间戳必须依赖 IANA 时区数据库，而不能只用固定的 UTC 偏移。",
                "例如：美国与欧洲大部分地区实行夏令时；日本、印度、中国、越南、菲律宾等不实行。",
            ),
        ),
        HelpArticle(
            id = "differences",
            titleEn = "Why time zones differ between countries",
            titleZh = "各国时区为何不同",
            paragraphsEn = listOf(
                "The world is divided into UTC offsets from −12 to +14, but many places use half-hour or quarter-hour steps (e.g. India UTC+5:30, Nepal UTC+5:45). Some countries span multiple zones; others share one offset across the whole nation. A few regions also change offset seasonally because of DST. Always pick the IANA zone name (such as Asia/Kolkata), not just “UTC+7”.",
            ),
            paragraphsZh = listOf(
                "全球 UTC 偏移从 −12 到 +14 不等，许多地区使用半时区或 quarter 时区（如印度 UTC+5:30、尼泊尔 UTC+5:45）。有的国家横跨多个时区，有的全国统一一个偏移；部分地区因夏令时而季节性变化。应选用 IANA 时区名（如 Asia/Kolkata），而非仅写「UTC+7」。",
            ),
        ),
        HelpArticle(
            id = "countries",
            titleEn = "India, Vietnam & the Philippines",
            titleZh = "印度、越南与菲律宾",
            paragraphsEn = listOf(
                "India — Asia/Kolkata: UTC+5:30 year-round (30-minute offset). India does not use DST. The same zone covers most of the country, including New Delhi and Mumbai.",
                "Vietnam — Asia/Ho_Chi_Minh: UTC+7 year-round. Vietnam abolished daylight saving in 1975 and has kept a fixed offset since. Ho Chi Minh City and Hanoi share this zone.",
                "Philippines — Asia/Manila: UTC+8 year-round, aligned with China, Singapore, and western Australia. The Philippines does not observe DST; Manila and the entire archipelago use this zone.",
            ),
            paragraphsZh = listOf(
                "印度 — Asia/Kolkata：全年 UTC+5:30（半时区偏移），不实行夏令时。该时区覆盖印度大部分地区，包括新德里和孟买。",
                "越南 — Asia/Ho_Chi_Minh：全年 UTC+7，1975 年起取消夏令时并保持固定偏移。胡志明市与河内均使用此时区。",
                "菲律宾 — Asia/Manila：全年 UTC+8，与中国、新加坡及澳大利亚西部对齐。菲律宾不实行夏令时，马尼拉及全国均使用此时区。",
            ),
        ),
        HelpArticle(
            id = "coverage",
            titleEn = "Regions supported by this tool",
            titleZh = "本工具支持的地区",
            paragraphsEn = listOf(
                "This converter works with standard IANA time zones on your device — common hubs such as Asia/Shanghai, America/New_York, Europe/London, and many more. The zone only changes how the same UTC instant is displayed.",
            ),
            paragraphsZh = listOf(
                "本转换器使用设备上的标准 IANA 时区——如 Asia/Shanghai、America/New_York、Europe/London 等常用商务枢纽。时区只改变同一 UTC 瞬间的钟面显示。",
            ),
        ),
        HelpArticle(
            id = "faq-epoch",
            titleEn = "What is epoch to date? How do I convert a timestamp to a date?",
            titleZh = "什么是 epoch to date？如何把时间戳转成日期？",
            paragraphsEn = listOf(
                "Epoch to date means turning a Unix epoch value into a human-readable date-time. Paste a seconds or milliseconds timestamp here; the local time field updates automatically. Edit the local time to convert back to a timestamp.",
            ),
            paragraphsZh = listOf(
                "epoch to date 就是把 Unix epoch（时间戳）转成可读日期时间。在本工具中填入秒级或毫秒级时间戳，本地时间会自动同步；修改本地时间则可反向得到时间戳。",
            ),
        ),
        HelpArticle(
            id = "faq-ms",
            titleEn = "Seconds vs milliseconds vs microseconds — what’s the difference?",
            titleZh = "秒、毫秒、微秒时间戳有什么区别？",
            paragraphsEn = listOf(
                "Second timestamps are usually 10 digits (e.g. 1735689600). Milliseconds are about 13 digits (JavaScript Date.now()). Microseconds add three more digits. Don’t mix units. This app auto-detects seconds vs milliseconds by digit length.",
            ),
            paragraphsZh = listOf(
                "秒级通常是 10 位（如 1735689600）；毫秒级约 13 位（JavaScript Date.now()）；微秒级再乘 1000。长度差很多时不要直接混用。本页按位数自动识别秒与毫秒。",
            ),
        ),
        HelpArticle(
            id = "faq-convert",
            titleEn = "How do I get the current Unix timestamp in code?",
            titleZh = "编程里怎么获取当前 Unix 时间戳？",
            paragraphsEn = listOf(
                "Open “Get current timestamp” from the links below for snippets in JavaScript, Python, Java, PHP, Go, SQL, and many more. Each snippet can be copied separately. For one-off conversions, use this screen instead of hand math.",
            ),
            paragraphsZh = listOf(
                "点下方链接「获取当前时间戳」可查看 JavaScript、Python、Java、PHP、Go、SQL 等常见写法，每段代码可单独复制。临时换算可直接用本页在线转换，避免时区算错。",
            ),
        ),
        HelpArticle(
            id = "faq-zone",
            titleEn = "Does a Unix timestamp change with time zone?",
            titleZh = "时间戳会随时区变化吗？",
            paragraphsEn = listOf(
                "No. Unix timestamps are UTC-based, so the same instant has the same number worldwide. The zone only changes the wall-clock display. Prefer timestamps for logs; pick a zone when showing times to people.",
            ),
            paragraphsZh = listOf(
                "不会。Unix 时间戳基于 UTC，全球同一瞬间数值相同。时区只改变「钟面显示」。日志对账请用时间戳；给人看时再选时区。",
            ),
        ),
        HelpArticle(
            id = "faq-y2k38",
            titleEn = "What is the Year 2038 problem?",
            titleZh = "2038 年问题是什么？",
            paragraphsEn = listOf(
                "Systems that store second timestamps in signed 32-bit integers overflow around 2038-01-19. Modern 64-bit platforms and millisecond timestamps are generally fine. Watch field types when talking to older systems.",
            ),
            paragraphsZh = listOf(
                "在仍用 32 位有符号整数存秒级时间戳的系统上，最大可表示时间约为 2038-01-19。现代 64 位系统与毫秒时间戳一般不受影响。对接老系统时注意字段类型。",
            ),
        ),
        HelpArticle(
            id = "related",
            titleEn = "What to try next",
            titleZh = "接下来还可以用这些工具",
            paragraphsEn = listOf(
                "After converting a timestamp, people often need: Time zone converter (same instant in other cities), Time interval (duration between two moments), Date offset (add/subtract calendar units), Age calculator (from a birth date).",
            ),
            paragraphsZh = listOf(
                "做完时间戳转换后，常需要：时区转换（对照各城市本地时间）、时间间隔（两个时间点相差多久）、日期推算（加减年/月/日或时分秒）、年龄计算（由出生日期算周岁与生肖星座）。",
            ),
        ),
    )

    fun article(id: String): HelpArticle? = linkArticles.find { it.id == id }

    const val SNIPPETS_LINK_ID = "snippets"
}
