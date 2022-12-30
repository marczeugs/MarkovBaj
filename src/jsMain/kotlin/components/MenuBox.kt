package components

import BuildInfo
import CompletedAchievement
import LocalStorageBackedSnapshotStateMap
import Styles
import achievements
import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.datetime.Instant
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun MenuBox(
    achievementCompletionMap: LocalStorageBackedSnapshotStateMap<Int, CompletedAchievement>
) {
    var expanded by remember { mutableStateOf(false) }

    Button(
        attrs = {
            classes(Styles.menuButton)

            onClick {
                expanded = !expanded
            }

            style {
                marginTop(if (expanded) 40.vh - Styles.bodyPadding else -Styles.bodyPadding)
            }
        }
    ) {
        Img(
            src = "img/menu_button.webp",
            attrs = { classes(Styles.menuButtonIcon) }
        )
    }

    Div(
        attrs = {
            classes(Styles.menuContainer)

            style {
                marginTop(if (expanded) -Styles.bodyPadding else (-40).vh - Styles.bodyPadding)
            }
        }
    ) {
        H1(attrs = { classes(Styles.menuHeadline) }) {
            Text("Achievements (${achievementCompletionMap.size}/${achievements.size})")
        }

        Div(attrs = { classes(Styles.menuText) }) {
            Text("You cannot earn achievements about getting Markov to say specific phrases by using the exact same phrases to prompt a similar response. Hovering over / clicking the achievement will give you a hint.")
        }

        for (achievement in achievements) {
            Div(attrs = {
                classes(Styles.achievementContainer)

                val achievementDescription = achievementCompletionMap[achievement.id]?.let {
                    """
                        ${achievement.description}
                        
                        Obtained: ${it.instant}
                        Query: "${it.query}"
                        Response: "${it.response}"
                    """.trimIndent()
                } ?: run {
                    achievement.description
                }

                title(achievementDescription)

                onClick {
                    window.alert(achievementDescription)
                }
            }) {
                Div(
                    attrs = {
                        classes(Styles.achievementIconContainer)

                        style {
                            backgroundColor(
                                if (achievementCompletionMap[achievement.id] != null) {
                                    Color.white
                                } else {
                                    rgb(50, 50, 50)
                                }
                            )
                        }
                    }
                ) {
                    Img(
                        src = "img/achievements/${achievement.id}.webp",
                        attrs = {
                            classes(Styles.achievementIcon)
                        }
                    )
                }

                Span(
                    attrs = {
                        classes(Styles.achievementCaption)

                        style {
                            color(
                                if (achievementCompletionMap[achievement.id] != null) {
                                    Color.white
                                } else {
                                    rgb(100, 100, 100)
                                }
                            )
                        }
                    }
                ) {
                    Text(achievement.name)
                }
            }
        }

        H1(attrs = { classes(Styles.menuHeadline) }) {
            Text("Credits")
        }

        Div(attrs = { classes(Styles.creditsLine) }) {
            Text("Design Lead / Visuals: 2pfrog")

            A(href = "https://twitter.com/2pfrog", attrs = { classes(Styles.socialMediaIcon); target(ATarget.Blank) }) {
                Img(src = "img/socialmedia/twitter.svg")
            }
        }

        Div(attrs = { classes(Styles.creditsLine) }) {
            Text("Software Development Lead: the_marcster")

            A(href = "https://github.com/marczeugs", attrs = { classes(Styles.socialMediaIcon); target(ATarget.Blank) }) {
                Img(src = "img/socialmedia/github.svg")
            }

            A(href = "https://www.twitch.tv/the_marcster", attrs = { classes(Styles.socialMediaIcon); target(ATarget.Blank) }) {
                Img(src = "img/socialmedia/twitch.svg")
            }
        }

        Div(
            attrs = {
                classes(Styles.creditsLine)

                style {
                    marginTop(24.px)
                }
            }
        ) {
            Text("Chief Stream Entertainment Leads: Scrafi1, ugworm_, okay_dudee, john7623, capybaraguy0, nishabtam, gakibas, toxcubed, Ramtinzx_, MrGreenMeme, AVlst, Peanut_Galaxy, FlamingDOTexe, Torrox_Morrox, racer4940, adamsero")
        }

        Div(
            attrs = {
                classes(Styles.menuText)

                style {
                    marginTop(48.px)
                }
            }
        ) {
            Text("MarkovBaj Version ${BuildInfo.PROJECT_VERSION}, Frontend Build ${Instant.fromEpochMilliseconds(BuildInfo.PROJECT_BUILD_TIMESTAMP_MILLIS)}")
        }
    }
}