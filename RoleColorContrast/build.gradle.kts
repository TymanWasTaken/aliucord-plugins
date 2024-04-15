version = "1.1.0"
description = "A plugin to improve contrast between role colors and the background"
aliucord {
    changelog.set("""
        # Version 1.1.0
        * Now supports names in the member list
        * Fixed a possible issue when themer wsa not installed
        * Contrast adjustments are now faster
        # Version 1.0.0
        * Initial release, supporting only names in chat
    """.trimIndent())
    excludeFromUpdaterJson.set(false)
}