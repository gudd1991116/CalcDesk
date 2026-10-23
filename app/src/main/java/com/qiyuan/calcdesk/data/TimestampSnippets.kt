package com.qiyuan.calcdesk.data

data class TimestampSnippetBlock(
    val label: String? = null,
    val code: String,
)

data class TimestampSnippetLang(
    val lang: String,
    val blocks: List<TimestampSnippetBlock>,
)

/** Ported from WebToolPage js/data/timestamp-snippets.js */
object TimestampSnippets {
    val all: List<TimestampSnippetLang> = listOf(
        TimestampSnippetLang("JavaScript", listOf(TimestampSnippetBlock(code = "Math.floor(Date.now() / 1000)"))),
        TimestampSnippetLang("TypeScript", listOf(TimestampSnippetBlock(code = "Math.floor(Date.now() / 1000)"))),
        TimestampSnippetLang("Node.js", listOf(TimestampSnippetBlock(code = "Math.floor(Date.now() / 1000)"))),
        TimestampSnippetLang(
            "Python",
            listOf(
                TimestampSnippetBlock("stdlib", "import time\ntime.time()"),
                TimestampSnippetBlock("Arrow", "import arrow\narrow.utcnow().int_timestamp"),
            ),
        ),
        TimestampSnippetLang(
            "Java",
            listOf(
                TimestampSnippetBlock("Java 8+", "Instant.now().getEpochSecond();"),
                TimestampSnippetBlock("pure Java", "System.currentTimeMillis() / 1000;"),
                TimestampSnippetBlock("Joda Time", "DateTime.now().getMillis() / 1000;"),
            ),
        ),
        TimestampSnippetLang(
            "Kotlin",
            listOf(
                TimestampSnippetBlock("epoch second", "Instant.now().epochSecond"),
                TimestampSnippetBlock("millis", "System.currentTimeMillis() / 1000"),
            ),
        ),
        TimestampSnippetLang("Scala", listOf(TimestampSnippetBlock(code = "System.currentTimeMillis() / 1000"))),
        TimestampSnippetLang("Go", listOf(TimestampSnippetBlock(code = "import \"time\"\n\nint64(time.Now().Unix())"))),
        TimestampSnippetLang(
            "Rust",
            listOf(
                TimestampSnippetBlock(
                    code = "use std::time::{SystemTime, UNIX_EPOCH};\n\nSystemTime::now()\n    .duration_since(UNIX_EPOCH)\n    .unwrap()\n    .as_secs()",
                ),
            ),
        ),
        TimestampSnippetLang(
            "C",
            listOf(
                TimestampSnippetBlock(
                    code = "#include <sys/time.h>\n\nstruct timeval tv;\ngettimeofday(&tv, NULL);\n// seconds: tv.tv_sec\n// milliseconds: tv.tv_sec * 1000LL + tv.tv_usec / 1000",
                ),
            ),
        ),
        TimestampSnippetLang(
            "C++",
            listOf(
                TimestampSnippetBlock(
                    code = "#include <chrono>\n\nstd::chrono::duration_cast<std::chrono::seconds>(\n    std::chrono::system_clock::now().time_since_epoch()\n).count()",
                ),
            ),
        ),
        TimestampSnippetLang("C# / .NET", listOf(TimestampSnippetBlock(code = "DateTimeOffset.UtcNow.ToUnixTimeSeconds();"))),
        TimestampSnippetLang("F#", listOf(TimestampSnippetBlock(code = "DateTimeOffset.UtcNow.ToUnixTimeSeconds()"))),
        TimestampSnippetLang("VB.NET", listOf(TimestampSnippetBlock(code = "DateTimeOffset.UtcNow.ToUnixTimeSeconds()"))),
        TimestampSnippetLang(
            "PHP",
            listOf(
                TimestampSnippetBlock("pure PHP", "<?php\ntime();"),
                TimestampSnippetBlock("Carbon", "<?php\nuse Carbon\\Carbon;\nCarbon::now()->timestamp;"),
            ),
        ),
        TimestampSnippetLang("Ruby", listOf(TimestampSnippetBlock(code = "Time.now.to_i"))),
        TimestampSnippetLang("Swift", listOf(TimestampSnippetBlock(code = "Int(Date().timeIntervalSince1970)"))),
        TimestampSnippetLang("Objective-C", listOf(TimestampSnippetBlock(code = "[[NSDate date] timeIntervalSince1970]"))),
        TimestampSnippetLang("Dart", listOf(TimestampSnippetBlock(code = "(DateTime.now().millisecondsSinceEpoch ~/ 1000)"))),
        TimestampSnippetLang("Shell / Bash", listOf(TimestampSnippetBlock(code = "date +%s"))),
        TimestampSnippetLang("PowerShell", listOf(TimestampSnippetBlock(code = "[int][double]::Parse((Get-Date -UFormat %s))"))),
        TimestampSnippetLang("Lua", listOf(TimestampSnippetBlock(code = "os.time()"))),
        TimestampSnippetLang("Groovy", listOf(TimestampSnippetBlock(code = "(new Date().time / 1000).longValue()"))),
        TimestampSnippetLang("Perl", listOf(TimestampSnippetBlock(code = "time"))),
        TimestampSnippetLang("R", listOf(TimestampSnippetBlock(code = "as.integer(Sys.time())"))),
        TimestampSnippetLang("Julia", listOf(TimestampSnippetBlock(code = "round(Int, time())"))),
        TimestampSnippetLang(
            "Erlang",
            listOf(
                TimestampSnippetBlock(
                    code = "calendar:datetime_to_gregorian_seconds(calendar:universal_time()) - 719528 * 24 * 3600.",
                ),
            ),
        ),
        TimestampSnippetLang("Elixir", listOf(TimestampSnippetBlock(code = "System.system_time(:second)"))),
        TimestampSnippetLang("Haskell", listOf(TimestampSnippetBlock(code = "floor . realToFrac <$> getPOSIXTime"))),
        TimestampSnippetLang("Clojure", listOf(TimestampSnippetBlock(code = "(quot (System/currentTimeMillis) 1000)"))),
        TimestampSnippetLang("OCaml", listOf(TimestampSnippetBlock(code = "Unix.time ()"))),
        TimestampSnippetLang("Crystal", listOf(TimestampSnippetBlock(code = "Time.utc.to_unix"))),
        TimestampSnippetLang("Nim", listOf(TimestampSnippetBlock(code = "getTime().toUnix()"))),
        TimestampSnippetLang("Zig", listOf(TimestampSnippetBlock(code = "std.time.timestamp()"))),
        TimestampSnippetLang("Delphi", listOf(TimestampSnippetBlock(code = "DateTimeToUnix(Now)"))),
        TimestampSnippetLang("MATLAB", listOf(TimestampSnippetBlock(code = "posixtime(datetime('now'))"))),
        TimestampSnippetLang("MySQL", listOf(TimestampSnippetBlock(code = "SELECT unix_timestamp(now())"))),
        TimestampSnippetLang("PostgreSQL", listOf(TimestampSnippetBlock(code = "SELECT extract(epoch from now())::bigint"))),
        TimestampSnippetLang("SQLite", listOf(TimestampSnippetBlock(code = "SELECT strftime('%s', 'now')"))),
        TimestampSnippetLang("Redis", listOf(TimestampSnippetBlock(code = "TIME"))),
    )
}
