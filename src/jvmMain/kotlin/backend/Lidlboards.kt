package backend

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.util.pipeline.*
import kotlinx.css.*
import kotlinx.css.properties.border
import kotlinx.datetime.Instant
import kotlinx.html.*

suspend fun PipelineContext<Unit, ApplicationCall>.lidlboards(
    latestTableValuesUpdateInstant: Instant?,
    tableValues: Map<String, Pair<List<String>, List<List<String>>>>
) {
    call.respondHtml {
        head {
            title("MarkovBaj Lidlboards")

            meta(name = "viewport", content = "width=device-width, initial-scale=1")

            style {
                unsafe {
                    +CssBuilder().apply {
                        "body" {
                            fontFamily = "Arial"
                            margin(0.px)
                            position = Position.relative
                        }

                        "div.header" {
                            position = Position.sticky
                            top = 0.px
                            backgroundColor = Color.cornflowerBlue
                            color = Color.white
                            display = Display.grid
                            gridTemplateColumns = GridTemplateColumns(1.fr, 1.fr)
                            gridTemplateRows = GridTemplateRows(1.fr)
                            alignItems = Align.center
                            paddingLeft = 32.px
                            paddingRight = 32.px
                            paddingTop = 16.px
                            paddingBottom = 16.px
                            wordBreak = WordBreak.breakWord
                        }

                        "div.content" {
                            padding(16.px)
                            display = Display.flex
                            gap = 16.px
                            alignItems = Align.flexStart
                            overflowX = Overflow.scroll
                        }

                        "table" {
                            minWidth = 500.px
                            border(1.px, BorderStyle.solid, Color.black)
                            borderCollapse = BorderCollapse.collapse
                        }

                        "button.expand-toggle" {
                            margin(8.px)
                            padding(8.px)
                            flexShrink = 0
                        }

                        "table.collapsed tr:not(:first-child)" {
                            display = Display.none
                        }

                        "tr" {
                            border(1.px, BorderStyle.solid, Color.black)
                        }

                        "th, td" {
                            textAlign = TextAlign.left
                            padding(8.px)
                        }

                        media("(max-width: 480px)") {
                            "table" {
                                width = 100.vw
                                minWidth = LinearDimension.auto
                                wordBreak = WordBreak.breakWord
                                fontSize = 10.pt
                            }

                            "th, td" {
                                padding(6.px)
                            }

                            "div.header" {
                                gridTemplateColumns = GridTemplateColumns(1.fr)
                                gridTemplateRows = GridTemplateRows("auto auto")
                                justifyItems = JustifyItems.center
                                paddingTop = 8.px
                                paddingBottom = 8.px
                            }

                            "div.header div" {
                                marginTop = 8.px
                                marginBottom = 8.px
                                put("text-align", "center !important")
                            }

                            "div.content" {
                                padding(8.px)
                                flexWrap = FlexWrap.wrap
                                justifyItems = JustifyItems.center
                                overflowX = Overflow.visible
                            }
                        }
                    }.toString()
                }
            }

            script {
                unsafe {
                    +"""
                        function updateExpandToggleButtons() {
                            for (const button of document.querySelectorAll('button.expand-toggle')) {
                                if (button.closest('table').classList.contains('collapsed')) {
                                    button.textContent = 'Expand';
                                } else {
                                    button.textContent = 'Collapse';
                                }
                            }
                        }
                        
                        addEventListener('DOMContentLoaded', _ => {
                            window.onresize();
                            updateExpandToggleButtons();
                        });
                        
                        let lastWindowWidth = window.innerWidth - 1;
                        
                        window.onresize = _ => {
                            if (window.innerWidth < 480 && window.innerWidth !== lastWindowWidth) {
                                for (const table of document.querySelectorAll('table')) {
                                    table.classList.add('collapsed');
                                }
                                
                                updateExpandToggleButtons();
                                
                                lastWindowWidth = window.innerWidth;
                            }
                        };
                    """.trimIndent()
                }
            }
        }

        body {
            div(classes = "header") {
                div {
                    style = "font-size: 32px;"
                    +"MarkovBaj Lidlboards"
                }

                div {
                    style = "text-align: right;"
                    +"Last updated at: $latestTableValuesUpdateInstant"
                }
            }

            div(classes = "content") {
                tableValues.forEach { (tableDisplayName, values) ->
                    val (headers, rows) = values

                    table {
                        tr {
                            th {
                                colSpan = headers.size.toString()

                                span {
                                    style = "display: flex; justify-content: space-between; align-items: center; padding-left: 16px;"

                                    +tableDisplayName

                                    button(classes = "expand-toggle") {
                                        onClick = "this.closest('table').classList.toggle('collapsed'); updateExpandToggleButtons();"
                                        +""
                                    }
                                }
                            }
                        }

                        tr {
                            headers.forEach { th { +it } }
                        }

                        rows.forEach { row ->
                            tr {
                                row.forEach {
                                    td {
                                        if (it.startsWith("https://")) {
                                            a(href = it) {
                                                +"Link"
                                            }
                                        } else {
                                            +it
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}