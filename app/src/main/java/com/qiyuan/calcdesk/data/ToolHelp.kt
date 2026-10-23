package com.qiyuan.calcdesk.data

data class ToolHelpContent(
    val summaryEn: String,
    val summaryZh: String,
    val sectionsEn: List<Pair<String, String>>, // title to body
    val sectionsZh: List<Pair<String, String>>,
)

fun ToolHelpContent.summary(zh: Boolean) = if (zh) summaryZh else summaryEn
fun ToolHelpContent.sections(zh: Boolean) = if (zh) sectionsZh else sectionsEn

object ToolHelp {

    private val fallback = ToolHelpContent(
        summaryEn = "This tool runs fully offline on your device.",
        summaryZh = "此工具完全在本机离线运行。",
        sectionsEn = listOf(
            "How to use" to "Fill in the input fields and the result updates or appears after you tap the action button. Use the copy button to copy a result.",
        ),
        sectionsZh = listOf(
            "使用方法" to "填写输入项后，结果会自动更新或在点击操作按钮后显示。可使用复制按钮复制结果。",
        ),
    )

    fun get(toolId: String): ToolHelpContent = contents[toolId] ?: fallback

    fun has(toolId: String): Boolean = contents.containsKey(toolId)

    private val contents: Map<String, ToolHelpContent> = mapOf(
        // ───────────── Date & time ─────────────
        "timestamp" to ToolHelpContent(
            summaryEn = TimestampHelp.aboutSummaryEn,
            summaryZh = TimestampHelp.aboutSummaryZh,
            sectionsEn = listOf(
                TimestampHelp.originTitleEn to TimestampHelp.originBodyEn,
                TimestampHelp.howToTitleEn to TimestampHelp.howToBodyEn,
            ),
            sectionsZh = listOf(
                TimestampHelp.originTitleZh to TimestampHelp.originBodyZh,
                TimestampHelp.howToTitleZh to TimestampHelp.howToBodyZh,
            ),
        ),
        "time-interval" to ToolHelpContent(
            summaryEn = "Find the difference between two points in time.",
            summaryZh = "计算两个时间点之间相差多久。",
            sectionsEn = listOf(
                "How to use" to "Enter a start and an end date and time. The result shows the difference in days, hours, minutes and seconds.",
                "Notes" to "If the end is earlier than the start, the result may be negative or reordered. Daylight-saving changes can make a local day 23 or 25 hours long.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入起始与结束的日期和时间，结果会以天、小时、分钟、秒显示两者的差值。",
                "注意事项" to "若结束时间早于起始时间，结果可能为负数或被自动调整。夏令时切换可能使某一天只有 23 或 25 小时。",
            ),
        ),
        "date-interval" to ToolHelpContent(
            summaryEn = "Count the days between two calendar dates.",
            summaryZh = "计算两个日期之间相差多少天。",
            sectionsEn = listOf(
                "How to use" to "Pick a start date and an end date. The tool shows the total number of days and a breakdown in years, months and days.",
                "Inclusive or not" to "Standard subtraction does not count the start date: 1 May to 2 May is 1 day. If your rule counts both ends (hotel stays, leave forms), add one day to the result.",
                "Leap years" to "Month lengths and leap years are handled by the calendar, so you do not need to approximate a month as 30 days.",
            ),
            sectionsZh = listOf(
                "使用方法" to "选择起始日期与结束日期，工具会显示总天数以及年、月、日的拆分。",
                "是否含首尾" to "常规相减不包含起始当天：5 月 1 日到 5 月 2 日为 1 天。若你的规则两端都算（如酒店住宿、请假），请在结果上加 1 天。",
                "闰年" to "月份天数与闰年均按日历规则处理，无需把一个月按 30 天估算。",
            ),
        ),
        "date-offset" to ToolHelpContent(
            summaryEn = "Add or subtract days from a date.",
            summaryZh = "从某个日期起向前或向后推算若干天。",
            sectionsEn = listOf(
                "How to use" to "Choose a base date, enter the number of days and pick add or subtract. The resulting date and weekday are shown.",
                "Notes" to "Calendar days are counted, including weekends and holidays. For business-day deadlines, check the result against your own holiday calendar.",
            ),
            sectionsZh = listOf(
                "使用方法" to "选择基准日期，输入天数并选择加或减，即可得到结果日期及星期。",
                "注意事项" to "按自然日计算，包含周末与节假日。若涉及工作日期限，请结合自己的节假日安排核对结果。",
            ),
        ),
        "age" to ToolHelpContent(
            summaryEn = "Calculate exact age from a birth date.",
            summaryZh = "根据出生日期计算精确年龄。",
            sectionsEn = listOf(
                "How to use" to "Enter a birth date. By default the age is measured to today; you can pick another date to see the age at that moment.",
                "What you get" to "The result shows full years, months and days, along with the total number of days lived and the days until the next birthday.",
                "Notes" to "People born on 29 February are usually treated as having their birthday on 28 Feb or 1 Mar in common years; legal rules vary by country.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入出生日期。默认计算到今天，也可以选择其他日期来查看当时的年龄。",
                "结果内容" to "结果包含周岁、月、日，以及已度过的总天数和距离下次生日的天数。",
                "注意事项" to "2 月 29 日出生者在平年通常按 2 月 28 日或 3 月 1 日过生日，具体法律规定因国家而异。",
            ),
        ),
        "timezone" to ToolHelpContent(
            summaryEn = "Compare the same moment across world cities.",
            summaryZh = "对照同一时刻在世界各主要城市的当地时间。",
            sectionsEn = listOf(
                "How to use" to "Choose a source city, date and time, then read the equivalent local times in other cities. Pick the actual meeting date, not just today's.",
                "Daylight saving" to "Offsets change with daylight saving time and some regions change clocks on different dates. Recheck recurring meetings around the March–April and October–November transitions.",
                "Tip" to "When sharing a time, include the city or zone name and the date, e.g. 22 Oct 14:00 Asia/Shanghai, instead of ambiguous abbreviations like CST or IST.",
            ),
            sectionsZh = listOf(
                "使用方法" to "选择出发城市、日期与时间，即可查看其他城市对应的当地时间。请使用实际会议日期，而不只是今天。",
                "夏令时" to "时差会随夏令时变化，不同地区切换日期也不同。涉及定期会议时，请在 3–4 月与 10–11 月前后重新核对。",
                "小提示" to "分享时间时请写明城市或时区与日期，例如“10 月 22 日 14:00 北京时间”，避免 CST、IST 这类有歧义的缩写。",
            ),
        ),
        "countdown" to ToolHelpContent(
            summaryEn = "See how many days remain until a target date.",
            summaryZh = "查看距离目标日期还剩多少天。",
            sectionsEn = listOf(
                "How to use" to "Pick a target date (and optionally a time). The tool shows the remaining days, hours and minutes.",
                "Notes" to "The countdown is calculated from your device clock and time zone. If the target is in the past, the time elapsed since then is shown.",
            ),
            sectionsZh = listOf(
                "使用方法" to "选择目标日期（可选时间），工具会显示剩余的天、小时与分钟。",
                "注意事项" to "倒计时基于设备时钟和时区计算。若目标日期已过，则显示已经过去的时间。",
            ),
        ),
        "stopwatch" to ToolHelpContent(
            summaryEn = "A simple stopwatch with lap times.",
            summaryZh = "带计次功能的简易秒表。",
            sectionsEn = listOf(
                "How to use" to "Tap Start to begin timing, Pause to stop, and Lap to record a split while it is running. Reset clears the time and all laps.",
                "Notes" to "Timing is based on the device's monotonic clock, but it may pause if the app is closed or the system suspends it. Keep the screen on for long measurements.",
            ),
            sectionsZh = listOf(
                "使用方法" to "点击开始计时，暂停停止；计时过程中点击计次可记录分段成绩。重置会清空时间和所有计次。",
                "注意事项" to "计时基于设备的单调时钟，但应用被关闭或系统挂起时可能中断。长时间计时请保持屏幕常亮。",
            ),
        ),

        // ───────────── Units ─────────────
        "unit-weight" to ToolHelpContent(
            summaryEn = "Convert between kilograms, pounds, ounces, jin and more.",
            summaryZh = "千克、磅、盎司、斤等重量单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a value, choose the source unit and the target unit. Use the swap button to reverse the direction.",
                "Notes" to "1 kg ≈ 2.2046 lb and 1 lb = 0.45359237 kg. One Chinese jin equals 500 g. Results are rounded for display, so use more decimals for precise work.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入数值，选择原单位与目标单位。可点击交换按钮反转换算方向。",
                "注意事项" to "1 千克 ≈ 2.2046 磅，1 磅 = 0.45359237 千克；1 市斤 = 500 克。结果为显示用四舍五入，精确计算请保留更多小数位。",
            ),
        ),
        "unit-length" to ToolHelpContent(
            summaryEn = "Convert metres, feet, inches, miles and other lengths.",
            summaryZh = "米、英尺、英寸、英里等长度单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a value and choose the source and target units. The converted value is shown instantly.",
                "Notes" to "1 inch is exactly 2.54 cm, 1 foot is 12 inches and 1 mile is 1.609344 km. Nautical miles differ from statute miles.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入数值并选择原单位与目标单位，即时显示换算结果。",
                "注意事项" to "1 英寸 = 2.54 厘米，1 英尺 = 12 英寸，1 英里 = 1.609344 千米。海里与普通英里不同，请勿混用。",
            ),
        ),
        "unit-temp" to ToolHelpContent(
            summaryEn = "Convert between Celsius, Fahrenheit and Kelvin.",
            summaryZh = "摄氏度、华氏度与开尔文互转。",
            sectionsEn = listOf(
                "Formulas" to "°F = °C × 9/5 + 32 and K = °C + 273.15. Water freezes at 0 °C (32 °F) and boils at 100 °C (212 °F) at sea level.",
                "Notes" to "Temperature conversion is not a simple ratio because the scales have different zero points. Values below absolute zero (−273.15 °C) are physically impossible.",
            ),
            sectionsZh = listOf(
                "换算公式" to "°F = °C × 9/5 + 32，K = °C + 273.15。标准大气压下水在 0 °C（32 °F）结冰，100 °C（212 °F）沸腾。",
                "注意事项" to "各温标零点不同，因此不能只靠乘一个比例换算。低于绝对零度（−273.15 °C）的数值在物理上不存在。",
            ),
        ),
        "unit-area" to ToolHelpContent(
            summaryEn = "Convert square metres, mu, hectares, acres and more.",
            summaryZh = "平方米、亩、公顷、英亩等面积单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a value and choose units. Swap reverses the direction.",
                "Notes" to "1 hectare = 10,000 m². One Chinese mu is about 666.67 m², and 1 acre is about 4,046.86 m². Square units grow with the square of length: 1 m² is 10,000 cm², not 100.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入数值并选择单位，可用交换按钮反转方向。",
                "注意事项" to "1 公顷 = 10,000 平方米；1 亩约 666.67 平方米；1 英亩约 4,046.86 平方米。面积按长度平方增长：1 平方米 = 10,000 平方厘米，而不是 100。",
            ),
        ),
        "unit-volume" to ToolHelpContent(
            summaryEn = "Convert litres, millilitres, gallons, cups and more.",
            summaryZh = "升、毫升、加仑、杯等体积单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a value and choose the source and target units.",
                "US vs UK" to "US and imperial (UK) gallons, pints and cups are different sizes. A US gallon is about 3.785 L while an imperial gallon is about 4.546 L. Check which one your recipe or fuel price uses.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入数值并选择原单位与目标单位。",
                "美制与英制" to "美制与英制的加仑、品脱、杯容量不同：1 美制加仑约 3.785 升，1 英制加仑约 4.546 升。请确认食谱或油价所用的制式。",
            ),
        ),
        "unit-speed" to ToolHelpContent(
            summaryEn = "Convert km/h, mph, m/s and knots.",
            summaryZh = "千米/时、英里/时、米/秒、节等速度单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a speed and pick the units. Useful for road signs abroad, wind speed and sports pace.",
                "Notes" to "1 m/s = 3.6 km/h, 1 mph ≈ 1.609 km/h and 1 knot ≈ 1.852 km/h. Mach is not a fixed speed because it depends on air temperature.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入速度并选择单位，适用于国外路牌、风速、运动配速等场景。",
                "注意事项" to "1 米/秒 = 3.6 千米/时，1 英里/时 ≈ 1.609 千米/时，1 节 ≈ 1.852 千米/时。马赫数随气温变化，并非固定速度。",
            ),
        ),
        "unit-storage" to ToolHelpContent(
            summaryEn = "Convert B, KB, MB, GB, TB and bits.",
            summaryZh = "B、KB、MB、GB、TB 等存储单位互转。",
            sectionsEn = listOf(
                "Decimal vs binary" to "Drive makers use decimal units (1 GB = 1,000,000,000 bytes) while many operating systems show binary units (1 GiB = 1,073,741,824 bytes). That is why a 500 GB drive shows less capacity on a computer.",
                "Bits and bytes" to "One byte equals 8 bits. Network speeds use bits (Mbps) while file sizes use bytes (MB), so divide Mbps by 8 to estimate MB/s.",
            ),
            sectionsZh = listOf(
                "十进制与二进制" to "硬盘厂商采用十进制（1 GB = 1,000,000,000 字节），而许多操作系统显示二进制单位（1 GiB = 1,073,741,824 字节），因此 500 GB 的硬盘在电脑上显示的容量更小。",
                "比特与字节" to "1 字节 = 8 比特。网速用比特（Mbps），文件大小用字节（MB），将 Mbps 除以 8 即可估算 MB/s。",
            ),
        ),
        "unit-angle" to ToolHelpContent(
            summaryEn = "Convert degrees, radians, gradians and turns.",
            summaryZh = "度、弧度、梯度、圈等角度单位互转。",
            sectionsEn = listOf(
                "How to use" to "Enter an angle and choose the units. Results update instantly.",
                "Notes" to "A full circle is 360° = 2π rad ≈ 6.2832 rad = 400 gradians = 1 turn. Calculators and programming languages usually expect radians for trigonometric functions.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入角度并选择单位，结果即时更新。",
                "注意事项" to "一整圈为 360° = 2π 弧度 ≈ 6.2832 弧度 = 400 梯度 = 1 圈。计算器和编程语言的三角函数通常使用弧度。",
            ),
        ),

        // ───────────── Math ─────────────
        "calc-basic" to ToolHelpContent(
            summaryEn = "Everyday arithmetic calculator.",
            summaryZh = "日常加减乘除计算器。",
            sectionsEn = listOf(
                "How to use" to "Tap the keys to enter numbers and operators, then press equals. Use clear to reset the expression.",
                "Notes" to "Multiplication and division are evaluated before addition and subtraction. Decimal arithmetic may show tiny rounding differences, such as 0.1 + 0.2.",
            ),
            sectionsZh = listOf(
                "使用方法" to "点击按键输入数字和运算符，按等号得出结果；清除键可重置表达式。",
                "注意事项" to "先乘除后加减。小数运算可能出现极小的舍入误差，例如 0.1 + 0.2。",
            ),
        ),
        "calc-sci" to ToolHelpContent(
            summaryEn = "Scientific calculator with trig, logs and powers.",
            summaryZh = "支持三角函数、对数和幂运算的科学计算器。",
            sectionsEn = listOf(
                "How to use" to "Enter an expression with functions such as sin, cos, tan, log, ln, square root and powers, then evaluate it.",
                "Angle mode" to "Check whether trig functions use degrees or radians before calculating; sin(90) is 1 in degrees but about 0.894 in radians.",
                "Notes" to "Use parentheses to control the order of operations. Invalid input such as dividing by zero or the log of a negative number shows an error.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入包含 sin、cos、tan、log、ln、开方、幂等函数的表达式后计算。",
                "角度模式" to "计算前请确认三角函数使用角度还是弧度：sin(90) 在角度制为 1，在弧度制约为 0.894。",
                "注意事项" to "可用括号控制运算顺序。除以零或对负数取对数等无效输入会提示错误。",
            ),
        ),
        "radix" to ToolHelpContent(
            summaryEn = "Convert numbers between binary, octal, decimal and hex.",
            summaryZh = "二进制、八进制、十进制、十六进制互转。",
            sectionsEn = listOf(
                "How to use" to "Type a number in any field and the others update. Binary uses 0–1, octal 0–7, decimal 0–9 and hexadecimal 0–9 and A–F.",
                "Notes" to "Digits invalid for the chosen base are rejected. Very large numbers may exceed the supported range. Negative numbers are shown with a minus sign, not two's complement.",
            ),
            sectionsZh = listOf(
                "使用方法" to "在任一输入框键入数字，其余进制会同步更新。二进制用 0–1，八进制 0–7，十进制 0–9，十六进制 0–9 与 A–F。",
                "注意事项" to "不属于该进制的字符会被拒绝；过大的数可能超出支持范围；负数以负号显示，而非补码。",
            ),
        ),
        "fraction" to ToolHelpContent(
            summaryEn = "Simplify fractions and convert them to decimals.",
            summaryZh = "分数约分，以及分数与小数互转。",
            sectionsEn = listOf(
                "How to use" to "Enter a numerator and denominator to see the simplified fraction and its decimal value, or enter a decimal to find a matching fraction.",
                "Notes" to "The denominator cannot be zero. Repeating decimals such as 0.333… are approximated, so the fraction may be a close match rather than exact.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入分子和分母，可得到约分后的分数及小数值；也可输入小数求对应分数。",
                "注意事项" to "分母不能为零。循环小数（如 0.333…）只能近似转换，得到的分数可能是近似值。",
            ),
        ),
        "percent" to ToolHelpContent(
            summaryEn = "Quick percentage, change and discount calculations.",
            summaryZh = "占比、增减幅度、折扣等百分比速算。",
            sectionsEn = listOf(
                "Modes" to "Find X% of a number, what percent one number is of another, or the percentage increase or decrease between two values.",
                "Notes" to "Percentage change is relative to the starting value: going from 50 to 75 is +50%, but going back from 75 to 50 is −33.3%. Percentage points and percent are different things.",
            ),
            sectionsZh = listOf(
                "计算模式" to "可计算某数的 X%、一个数占另一个数的百分比，以及两个数之间的增减幅度。",
                "注意事项" to "变化幅度以起始值为基准：从 50 涨到 75 是 +50%，从 75 回落到 50 则是 −33.3%。百分点与百分比是不同概念。",
            ),
        ),
        "compound" to ToolHelpContent(
            summaryEn = "Estimate compound growth of a principal over time.",
            summaryZh = "估算本金在复利下随时间的增长。",
            sectionsEn = listOf(
                "How to use" to "Enter the principal, the annual rate, the number of years and the compounding frequency. The tool shows the final amount and the interest earned.",
                "Notes" to "Real returns fluctuate and fees, taxes and inflation reduce them. A small rate difference compounds into a large gap over many years.",
                "Disclaimer" to "Results are mathematical illustrations only and are not financial advice or a promise of returns.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入本金、年化利率、年数和计息频率，工具会显示期末本息及所得利息。",
                "注意事项" to "实际收益会波动，费用、税费和通货膨胀都会降低收益。利率的微小差距经长期复利会拉开很大差距。",
                "免责声明" to "结果仅为数学演示，不构成理财建议，也不代表收益承诺。",
            ),
        ),
        "random" to ToolHelpContent(
            summaryEn = "Generate random numbers or pick from your own list.",
            summaryZh = "生成随机数，或从自定义词语中随机抽取。",
            sectionsEn = listOf(
                "Numbers" to "Set a minimum, a maximum and how many numbers to draw. Both ends are included in the range.",
                "Custom picks" to "Enter items separated by commas or new lines and let the tool choose one at random, for example to decide where to eat.",
                "Notes" to "Results come from the device's pseudo-random generator, which is fine for games and decisions but should not be used for security-sensitive purposes. Use the Password tool for those.",
            ),
            sectionsZh = listOf(
                "随机数" to "设置最小值、最大值与抽取个数，范围包含两端的值。",
                "自定义抽取" to "以逗号或换行分隔输入多个选项，工具会随机选出其中之一，例如决定吃什么。",
                "注意事项" to "结果由设备的伪随机数生成器产生，适合游戏和日常决定，但不应用于安全敏感场景，此类场景请使用“密码生成”工具。",
            ),
        ),

        // ───────────── Developer ─────────────
        "java-formatter" to ToolHelpContent(
            summaryEn = "Indent and tidy Java source code locally.",
            summaryZh = "在本地对 Java 代码进行缩进与整理。",
            sectionsEn = listOf(
                "How to use" to "Paste Java code into the input box and tap Format. The result uses consistent indentation based on braces.",
                "Notes" to "This is a lightweight formatter, not a full parser: it does not reorder imports or fix syntax errors. Code never leaves your device.",
            ),
            sectionsZh = listOf(
                "使用方法" to "将 Java 代码粘贴到输入框，点击格式化，即可按花括号层级统一缩进。",
                "注意事项" to "这是轻量级格式化工具，并非完整语法解析：不会整理 import 顺序，也不会修复语法错误。代码不会离开你的设备。",
            ),
        ),
        "json-formatter" to ToolHelpContent(
            summaryEn = "Pretty-print, minify and validate JSON.",
            summaryZh = "JSON 格式化、压缩与校验。",
            sectionsEn = listOf(
                "How to use" to "Paste JSON and choose Format for readable output, Minify to remove whitespace, or Validate to check syntax. Errors describe what went wrong.",
                "Common errors" to "JSON requires double quotes around keys and strings, forbids trailing commas and does not allow comments. Single quotes or unquoted keys will fail validation.",
                "Privacy" to "Parsing happens on your device. Avoid sharing the output if it contains tokens or personal data.",
            ),
            sectionsZh = listOf(
                "使用方法" to "粘贴 JSON 后，选择格式化获得易读输出，压缩去除空白，或校验检查语法。错误信息会说明问题所在。",
                "常见错误" to "JSON 的键和字符串必须使用双引号，不允许末尾多余逗号，也不允许注释。单引号或未加引号的键都无法通过校验。",
                "隐私" to "解析在本机完成。若内容包含令牌或个人信息，请勿随意分享输出结果。",
            ),
        ),
        "regex" to ToolHelpContent(
            summaryEn = "Test regular expressions and replace matches.",
            summaryZh = "测试正则表达式并替换匹配内容。",
            sectionsEn = listOf(
                "How to use" to "Enter a pattern, choose flags such as ignore-case or multiline, and paste the text to test. Matches and capture groups are listed, and you can try a replacement string.",
                "Notes" to "The engine follows Java/Kotlin regex syntax, which differs slightly from JavaScript or Python (for example in named groups and some escapes). Remember to escape special characters such as . ? * + ( ) [ ] \\ when matching them literally.",
                "Performance" to "Patterns with nested quantifiers can run very slowly on long input. Keep patterns as specific as possible.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入正则表达式，选择忽略大小写、多行等标志，再粘贴待测文本。匹配项和捕获组会被列出，也可以尝试替换字符串。",
                "注意事项" to "引擎遵循 Java/Kotlin 正则语法，与 JavaScript、Python 在命名分组、部分转义上略有差异。要按字面匹配 . ? * + ( ) [ ] \\ 等特殊字符时需转义。",
                "性能" to "嵌套量词的表达式在长文本上可能非常慢，请尽量写得具体。",
            ),
        ),

        // ───────────── Life ─────────────
        "bmi" to ToolHelpContent(
            summaryEn = "Estimate body mass index from height and weight.",
            summaryZh = "根据身高与体重估算身体质量指数。",
            sectionsEn = listOf(
                "How to use" to "Enter your height and weight in the units you prefer. BMI = weight (kg) ÷ height (m)², and the result is matched to a category range.",
                "Reading the result" to "Adult categories commonly used are: under 18.5 underweight, 18.5–24.9 normal, 25–29.9 overweight and 30 or above obese. Some regions, including China, use slightly different cut-offs.",
                "Limits" to "BMI does not measure body fat, muscle, age, sex or fat distribution, and does not apply well to children, pregnancy or athletes.",
                "Disclaimer" to "For reference only — not medical advice. Consult a healthcare professional for health decisions.",
            ),
            sectionsZh = listOf(
                "使用方法" to "按所选单位输入身高与体重。BMI = 体重(kg) ÷ 身高(m)²，结果会对应到相应区间。",
                "如何解读" to "成人常用分类：低于 18.5 偏瘦，18.5–24.9 正常，25–29.9 超重，30 及以上肥胖。包括中国在内的一些地区使用略有不同的分界值。",
                "局限" to "BMI 无法反映体脂、肌肉、年龄、性别和脂肪分布，对儿童、孕期和运动员参考价值有限。",
                "免责声明" to "仅供参考，不能替代医疗建议。健康相关决定请咨询专业医生。",
            ),
        ),
        "fuel" to ToolHelpContent(
            summaryEn = "Calculate fuel consumption and trip cost.",
            summaryZh = "计算油耗并估算行程油费。",
            sectionsEn = listOf(
                "How to use" to "Enter the distance driven and the fuel used to get consumption in L/100 km, or enter consumption, distance and fuel price to estimate trip cost.",
                "Tip" to "For an accurate figure, fill the tank to the same level at both refuels and divide the fuel added by the distance since the first fill.",
                "Notes" to "Real consumption varies with speed, traffic, load, air conditioning and weather. Treat the result as an estimate.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入行驶里程和耗油量，得到百公里油耗；或输入油耗、里程与油价，估算行程油费。",
                "小技巧" to "为了更准确，两次加油请加到相同油位，用后一次加的油量除以两次加油间的里程。",
                "注意事项" to "实际油耗受车速、路况、载重、空调和天气影响，结果仅为估算。",
            ),
        ),
        "download-time" to ToolHelpContent(
            summaryEn = "Estimate how long a download will take.",
            summaryZh = "按网速与文件大小估算下载所需时间。",
            sectionsEn = listOf(
                "How to use" to "Enter the file size and its unit, then the connection speed and its unit. The tool shows the estimated time.",
                "Mbps vs MB/s" to "Mbps means megabits per second and MB/s means megabytes per second. One byte is 8 bits, so a 100 Mbps line peaks at about 12.5 MB/s.",
                "Real-world speed" to "Protocol overhead, Wi-Fi quality, server limits and other traffic usually reduce speed. A practical estimate is 80–90% of the rated speed.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入文件大小及单位，再输入网速及单位，工具会显示预计耗时。",
                "Mbps 与 MB/s" to "Mbps 是每秒兆比特，MB/s 是每秒兆字节。1 字节 = 8 比特，因此 100 Mbps 的网络峰值约为 12.5 MB/s。",
                "实际速度" to "协议开销、Wi-Fi 质量、服务器限速和其他流量通常会拉低速度，实际可按标称速度的 80–90% 估算。",
            ),
        ),
        "tip" to ToolHelpContent(
            summaryEn = "Work out the tip and split the bill per person.",
            summaryZh = "计算小费并按人数分摊账单。",
            sectionsEn = listOf(
                "How to use" to "Enter the bill amount, choose a tip percentage and the number of people. The tool shows the tip, the total and the amount per person.",
                "Notes" to "Tipping customs differ by country, and some places include a service charge already. Check your bill first to avoid paying twice.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入账单金额，选择小费比例和人数，工具会显示小费、总额与人均金额。",
                "注意事项" to "各国小费习惯不同，部分地区账单已包含服务费，请先查看账单以免重复支付。",
            ),
        ),
        "discount" to ToolHelpContent(
            summaryEn = "Calculate the sale price and how much you save.",
            summaryZh = "计算折后价与节省的金额。",
            sectionsEn = listOf(
                "How to use" to "Enter the original price and the discount percentage. The tool shows the final price and the amount saved.",
                "Notes" to "Stacked discounts do not simply add up: 20% off followed by 10% off equals 28% off in total, not 30%. In some regions a discount of 8 (折) means paying 80%.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入原价和折扣百分比，工具会显示折后价与节省金额。",
                "注意事项" to "叠加折扣不能直接相加：先减 20% 再减 10%，总共是减 28% 而非 30%。在部分地区“8 折”表示支付原价的 80%。",
            ),
        ),
        "due-date" to ToolHelpContent(
            summaryEn = "Estimate a due date from the last menstrual period.",
            summaryZh = "根据末次月经日期推算预产期。",
            sectionsEn = listOf(
                "How to use" to "Enter the first day of the last menstrual period (LMP) and, if your cycle is not 28 days, the cycle length. The tool estimates the due date using Naegele's rule (LMP + 280 days).",
                "Notes" to "Only a small share of babies arrive exactly on the estimated date; delivery between 37 and 42 weeks is considered normal. Ultrasound dating may adjust the estimate.",
                "Disclaimer" to "For reference only — not medical advice. Follow your doctor or midwife for prenatal care and dating.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入末次月经第一天的日期；若月经周期不是 28 天，可填写周期长度。工具按内格勒法则（末次月经 + 280 天）推算预产期。",
                "注意事项" to "只有少数宝宝恰好在预产期当天出生，孕 37–42 周分娩均属正常。B 超核对孕周后预产期可能调整。",
                "免责声明" to "仅供参考，不能替代医疗建议。产检与孕周核定请遵医嘱。",
            ),
        ),
        "mortgage" to ToolHelpContent(
            summaryEn = "Estimate the monthly payment and total interest of a loan.",
            summaryZh = "估算贷款的月供与利息总额。",
            sectionsEn = listOf(
                "How to use" to "Enter the loan amount, annual interest rate and term in years. The tool calculates an equal-instalment (annuity) monthly payment, total interest and total repaid.",
                "Notes" to "Real loans may use floating rates, different repayment methods (such as equal principal), fees, insurance or prepayment rules that are not included here.",
                "Disclaimer" to "For reference only — not financial advice. Confirm exact figures with your lender.",
            ),
            sectionsZh = listOf(
                "使用方法" to "输入贷款金额、年利率和年限，工具按等额本息方式计算月供、利息总额与还款总额。",
                "注意事项" to "实际贷款可能采用浮动利率、不同还款方式（如等额本金），并涉及手续费、保险或提前还款规则，这些均未计入。",
                "免责声明" to "仅供参考，不构成理财建议。具体金额请以贷款机构为准。",
            ),
        ),
        "password" to ToolHelpContent(
            summaryEn = "Generate strong random passwords.",
            summaryZh = "一键生成安全的随机密码。",
            sectionsEn = listOf(
                "How to use" to "Choose the length and which character types to include (lowercase, uppercase, digits, symbols), then generate and copy the password.",
                "Good practice" to "Use at least 12–16 characters and a different password for every account, ideally stored in a password manager. Length matters more than complexity rules.",
                "Privacy" to "Passwords are generated on your device and are not saved or sent anywhere. Clear the clipboard after pasting.",
            ),
            sectionsZh = listOf(
                "使用方法" to "选择长度及要包含的字符类型（小写、大写、数字、符号），然后生成并复制密码。",
                "使用建议" to "建议至少 12–16 位，且每个账号使用不同密码，最好配合密码管理器保存。长度比复杂规则更重要。",
                "隐私" to "密码在本机生成，不会被保存或上传。粘贴后建议清空剪贴板。",
            ),
        ),
        "wordcount" to ToolHelpContent(
            summaryEn = "Count words, characters and estimate reading time.",
            summaryZh = "统计中英文字数并估算阅读时长。",
            sectionsEn = listOf(
                "How to use" to "Paste or type text. The tool counts characters (with and without spaces), English words, Chinese characters, lines and an estimated reading time.",
                "Notes" to "Chinese is counted per character while English is counted per word. Different platforms count punctuation and spaces differently, so check the rule of your target site.",
            ),
            sectionsZh = listOf(
                "使用方法" to "粘贴或输入文本，工具会统计字符数（含/不含空格）、英文单词数、汉字数、行数，并估算阅读时长。",
                "注意事项" to "中文按字计数，英文按单词计数。不同平台对标点和空格的计法不同，请以目标平台的规则为准。",
            ),
        ),
    )
}
