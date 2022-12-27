import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.filter
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text


@Composable
fun AchievementsBox(
    achievementCompletionMap: LocalStorageBackedSnapshotStateMap<String, Boolean>
) {
    Div(attrs = { classes(Styles.achievementsBoxContainer) }) {
        Div(attrs = { classes(Styles.achievementExplanation) }) {
            Text("You cannot earn achievements about getting Markov to say specific phrases by using the exact same phrases to prompt a similar response.")
        }

        for (achievement in achievements) {
            Div(attrs = {
                classes(Styles.achievementContainer)
                title(achievement.description)
            }) {
                Div(
                    attrs = {
                        classes(Styles.achievementIconContainer)
                        title(achievement.description)

                        style {
                            backgroundColor(achievement.color)

                            if (achievementCompletionMap[achievement.name] != true) {
                                filter {
                                    grayscale(1)
                                }
                            }
                        }
                    }
                ) {
                    Img(
                        src = achievement.icon,
                        attrs = {
                            classes(Styles.achievementIcon)
                            title(achievement.description)

                            style {
                                if (achievementCompletionMap[achievement.name] != true) {
                                    filter {
                                        grayscale(1)
                                    }
                                }
                            }
                        }
                    )
                }

                Span(attrs = { classes(Styles.achievementCaption) }) {
                    Text(achievement.name)
                }
            }
        }
    }
}