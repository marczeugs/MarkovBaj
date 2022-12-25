import org.jetbrains.compose.web.css.*

object Styles : StyleSheet() {
    val rootElement by style {
        width(100.percent)
        maxWidth(768.px)
        height(100.vh)

        margin("auto" as CSSNumeric)
        padding(16.px)

        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)

        boxSizing("border-box")
        fontFamily("Helvetica")
    }

    val hidden by style {
        display(DisplayStyle.None)
    }

    val markovContainer by style {
        position(Position.Relative)
        height(40.vh)
    }

    val markovBodyPart by style {
        position(Position.Absolute)
        width(100.percent)
        height(100.percent)
        property("object-fit", "contain")
    }

    val markovHeadPart by style {
        /*animation(swayAnimation.provideDelegate(Styles, ::swayAnimation).getValue(Styles, ::swayAnimation)) {
            duration(3.s)
            timingFunction(AnimationTimingFunction.EaseInOut)
            iterationCount(Int.MAX_VALUE)
            direction(AnimationDirection.Alternate)
        }

        animation(bobAnimation.provideDelegate(Styles, ::bobAnimation).getValue(Styles, ::bobAnimation)) {
            duration(1.1.s)
            timingFunction(AnimationTimingFunction.EaseInOut)
            iterationCount(Int.MAX_VALUE)
            direction(AnimationDirection.Alternate)
        }*/
    }

    private val swayAnimation = keyframes {
        from {
            marginLeft((-1).percent)
        }

        to {
            marginTop(1.percent)
        }
    }

    private val bobAnimation = keyframes {
        from {
            marginTop((-0.3).percent)
        }

        to {
            marginTop(0.3.percent)
        }
    }

    val chatMessageContainer by style {
        width(100.percent)
        flex(1)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        overflow("scroll")
    }

    val chatMessage by style {
        margin(12.px)
        padding(20.px)
        borderRadius(16.px)
        boxSizing("border-box")
        width("fit-content" as CSSNumeric)
        maxWidth(40.percent)
        fontSize(16.px)
    }

    val chatMessageMarkov by style {
        backgroundColor(rgb(0xdd, 0xdd, 0xdd))
    }

    val chatMessageUser by style {
        backgroundColor(rgb(0xaa, 0xee, 0x99))
        alignSelf("end")
    }

    val chatMessageError by style {
        color(rgb(0xcc, 0x00, 0x00))
    }

    val loadingIcon by style {
        width(36.px)
    }

    val chatInput by style {
        width(100.percent)
        boxSizing("border-box")
        borderRadius(12.px)
        backgroundColor(rgb(0xee, 0xee, 0xee))
        border(1.px, LineStyle.Solid, Color.black)
        fontSize(16.px)
        padding(12.px)
        boxSizing("border-box")
    }
}