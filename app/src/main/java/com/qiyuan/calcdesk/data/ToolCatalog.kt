package com.qiyuan.calcdesk.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategoryId { Datetime, Units, Math, Dev, Life }

data class ToolCategory(
    val id: ToolCategoryId,
    val nameEn: String,
    val nameZh: String,
    val tools: List<ToolItem>,
)

data class ToolItem(
    val id: String,
    val nameEn: String,
    val nameZh: String,
    val descEn: String,
    val descZh: String,
    val icon: ImageVector,
    val category: ToolCategoryId,
    val healthDisclaimer: Boolean = false,
    val financeDisclaimer: Boolean = false,
)

object ToolCatalog {
    val categories: List<ToolCategory> = listOf(
        ToolCategory(
            ToolCategoryId.Datetime, "Date & time", "时间与日期",
            listOf(
                tool("timestamp", "Timestamp", "时间戳转换", "Unix epoch ↔ date", "时间与 Unix 时间戳互转", Icons.Outlined.AccessTime, ToolCategoryId.Datetime),
                tool("time-interval", "Time interval", "时间间隔", "Difference between two times", "两个时间点相差多久", Icons.Outlined.HourglassBottom, ToolCategoryId.Datetime),
                tool("date-interval", "Date interval", "日期间隔", "Days between dates", "两个日期之间的天数差", Icons.Outlined.CalendarMonth, ToolCategoryId.Datetime),
                tool("date-offset", "Date offset", "日期推算", "Add or subtract days", "从某日起向前或向后推算", Icons.Outlined.Event, ToolCategoryId.Datetime),
                tool("age", "Age", "年龄计算", "Exact age from birth date", "根据出生日期计算精确年龄", Icons.Outlined.Cake, ToolCategoryId.Datetime),
                tool("timezone", "Time zones", "时区转换", "World city time zones", "世界主要城市时区对照", Icons.Outlined.Public, ToolCategoryId.Datetime),
                tool("countdown", "Countdown", "倒计时", "Days until a date / anniversary", "距离目标日期还剩多少天", Icons.Outlined.Timer, ToolCategoryId.Datetime),
                tool("stopwatch", "Stopwatch", "秒表", "Start, pause, and laps", "开始、暂停与计次", Icons.Outlined.Timer, ToolCategoryId.Datetime),
            ),
        ),
        ToolCategory(
            ToolCategoryId.Units, "Units", "单位换算",
            listOf(
                tool("unit-weight", "Weight", "重量换算", "kg, lb, oz, jin…", "千克、磅、盎司等互转", Icons.Outlined.FitnessCenter, ToolCategoryId.Units),
                tool("unit-length", "Length", "长度换算", "m, ft, in, mi…", "米、英尺、英寸等互转", Icons.Outlined.Straighten, ToolCategoryId.Units),
                tool("unit-temp", "Temperature", "温度换算", "°C, °F, K", "摄氏、华氏、开尔文互转", Icons.Outlined.Thermostat, ToolCategoryId.Units),
                tool("unit-area", "Area", "面积换算", "m², mu, acre…", "平方米、亩、公顷等互转", Icons.Outlined.SquareFoot, ToolCategoryId.Units),
                tool("unit-volume", "Volume", "体积换算", "L, gal, cup…", "升、毫升、加仑等互转", Icons.Outlined.WaterDrop, ToolCategoryId.Units),
                tool("unit-speed", "Speed", "速度换算", "km/h, mph, knot…", "千米/时、英里/时等互转", Icons.Outlined.Speed, ToolCategoryId.Units),
                tool("unit-storage", "Storage", "存储换算", "B, KB, MB, GB…", "B、KB、MB、GB 互转", Icons.Outlined.Storage, ToolCategoryId.Units),
                tool("unit-angle", "Angle", "角度换算", "deg, rad, turn…", "度、弧度、梯度互转", Icons.Outlined.Architecture, ToolCategoryId.Units),
            ),
        ),
        ToolCategory(
            ToolCategoryId.Math, "Math", "数字计算",
            listOf(
                tool("calc-basic", "Calculator", "标准计算器", "Everyday arithmetic", "加减乘除与常用运算", Icons.Outlined.Calculate, ToolCategoryId.Math),
                tool("calc-sci", "Scientific", "科学计算器", "Trig, log, powers", "三角函数、对数、幂运算", Icons.Outlined.Science, ToolCategoryId.Math),
                tool("radix", "Radix", "进制换算", "Bin / oct / dec / hex", "二、八、十、十六进制互转", Icons.Outlined.DeviceHub, ToolCategoryId.Math),
                tool("fraction", "Fractions", "分数换算", "Simplify & convert", "分数与小数互转、约分", Icons.Outlined.Analytics, ToolCategoryId.Math),
                tool("percent", "Percent", "百分比", "Part, change, discount", "占比、增减幅、折扣速算", Icons.Outlined.Percent, ToolCategoryId.Math),
                tool("compound", "Compound interest", "复利计算", "Principal & yield", "本息、年化收益与期数", Icons.Outlined.Analytics, ToolCategoryId.Math, finance = true),
                tool("random", "Random", "随机数", "Numbers & custom picks", "数字与自定义词语随机抽取", Icons.Outlined.Casino, ToolCategoryId.Math),
            ),
        ),
        ToolCategory(
            ToolCategoryId.Dev, "Developer", "开发工具",
            listOf(
                tool("java-formatter", "Java formatter", "Java 格式化", "Indent Java locally", "本地格式化 Java 代码", Icons.Outlined.Code, ToolCategoryId.Dev),
                tool("json-formatter", "JSON formatter", "JSON 格式化", "Pretty / minify / validate", "格式化、压缩与校验", Icons.Outlined.DataObject, ToolCategoryId.Dev),
                tool("regex", "Regex tester", "正则测试", "Match & replace", "在线测试与替换", Icons.Outlined.Code, ToolCategoryId.Dev),
            ),
        ),
        ToolCategory(
            ToolCategoryId.Life, "Life", "生活实用",
            listOf(
                tool("bmi", "BMI", "BMI 指数", "Body mass index", "身体质量指数与健康区间", Icons.Outlined.FitnessCenter, ToolCategoryId.Life, health = true),
                tool("fuel", "Fuel", "油耗计算", "L/100km & trip cost", "百公里油耗与油费估算", Icons.Outlined.LocalGasStation, ToolCategoryId.Life),
                tool("download-time", "Download time", "下载时间", "ETA from speed & file size", "按网速与文件大小估算下载要多久", Icons.Outlined.CloudDownload, ToolCategoryId.Life),
                tool("tip", "Tip split", "小费分摊", "Tip and per-person", "聚餐小费与人均分摊", Icons.Outlined.Restaurant, ToolCategoryId.Life),
                tool("discount", "Discount", "折扣计算", "Sale price & savings", "折后价与节省金额", Icons.Outlined.LocalOffer, ToolCategoryId.Life),
                tool("due-date", "Due date", "预产期", "From LMP (reference)", "根据末次月经推算预产期", Icons.Outlined.ChildCare, ToolCategoryId.Life, health = true),
                tool("mortgage", "Mortgage", "房贷计算", "Equal payment / principal", "等额本息与等额本金对比", Icons.Outlined.Home, ToolCategoryId.Life, finance = true),
                tool("password", "Password", "密码生成", "Strong random passwords", "安全随机密码一键生成", Icons.Outlined.Lock, ToolCategoryId.Life),
                tool("wordcount", "Word count", "字数统计", "CJK & English stats", "中英文字数与阅读时长", Icons.Outlined.TextFields, ToolCategoryId.Life),
            ),
        ),
    )

    val allTools: List<ToolItem> = categories.flatMap { it.tools }

    fun find(id: String): ToolItem? = allTools.find { it.id == id }

    private fun tool(
        id: String,
        nameEn: String,
        nameZh: String,
        descEn: String,
        descZh: String,
        icon: ImageVector,
        category: ToolCategoryId,
        health: Boolean = false,
        finance: Boolean = false,
    ) = ToolItem(id, nameEn, nameZh, descEn, descZh, icon, category, health, finance)
}

fun ToolItem.displayName(zh: Boolean) = if (zh) nameZh else nameEn
fun ToolItem.displayDesc(zh: Boolean) = if (zh) descZh else descEn
fun ToolCategory.displayName(zh: Boolean) = if (zh) nameZh else nameEn
