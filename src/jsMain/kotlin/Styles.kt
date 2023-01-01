import org.jetbrains.compose.web.css.*

object Styles : StyleSheet() {
    val bodyPadding = 16.px


    init {
        "body" style {
            margin(0.px)
            backgroundImage("url('img/background.webp')")
            backgroundRepeat("repeat")
            backgroundSize("contain")
            color(Color.white)
            property("overscroll-behavior", "contain")
        }

        "::-webkit-scrollbar" style {
            display(DisplayStyle.None)
        }

        "input:focus" style {
            outline("none")
        }
    }


    val rootElement by style {
        position(Position.Relative)

        width(100.percent)
        maxWidth(768.px)
        height(100.vh)

        // Shitty hack to fix 100vh excluding URL bar on mobile devices, see: https://stackoverflow.com/questions/52848856/100vh-height-when-address-bar-is-shown-chrome-mobile
        property("height", "calc(var(--vh, 1vh) * 100)")

        property("margin", "auto")
        padding(bodyPadding)

        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)

        boxSizing("border-box")
        fontFamily("Consolas", "monospace")
    }

    val hidden by style {
        opacity(0)
    }

    val smallBorderContainer by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("20px auto 20px")
        gridTemplateRows("15px auto 15px")
        width(100.percent)
    }

    val smallBorderHorizontalImage by style {
        width(100.percent)
        height(69.px)
    }

    val notificationContainer by style {
        position(Position.Absolute)
        left(64.px)
        right(128.px)
        backgroundColor(rgba(50, 50, 50, 0.8))
        display(DisplayStyle.Flex)
        justifyItems("center")
    }

    val notificationInnerContainer by style {
        property("z-index", 3)
    }

    val notificationBackground by style {
        position(Position.Absolute)
        left(10.px)
        right(10.px)
        top(10.px)
        bottom(10.px)
        backgroundColor(rgb(60, 60, 60))
        property("z-index", 3)
    }

    val notificationContent by style {
        fontSize(16.px)
        gridColumn("2")
        gridRow("2")
        property("z-index", 3)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
    }

    val menuButton by style {
        position(Position.Absolute)
        right(bodyPadding)
        padding(8.px)
        property("z-index", 1)
        property("border", "none")
        backgroundColor(rgb(30, 30, 30))
        borderRadius(bottomLeft = 8.px, bottomRight = 8.px, topLeft = 0.px, topRight = 0.px)
        cursor("pointer")
    }

    val menuButtonIcon by style {
        width(36.px)
    }

    val menuContainer by style {
        position(Position.Absolute)
        display(DisplayStyle.Flex)
        height(40.vh)
        left(0.px)
        right(0.px)
        padding(16.px)
        backgroundColor(rgba(20, 20, 20, 0.9))
        property("z-index", 1)
        flexWrap(FlexWrap.Wrap)
        property("place-content", "flex-start")
        justifyContent(JustifyContent.SpaceBetween)
        overflowY("scroll")
        boxSizing("border-box")
        color(Color.white)
        borderRadius(bottomLeft = 8.px, bottomRight = 8.px, topLeft = 0.px, topRight = 0.px)
    }

    val menuHeadline by style {
        flexBasis(100.percent)
        padding(16.px)
        textAlign("justify")
        fontSize(18.pt)
        marginBottom(0.px)
    }

    val menuText by style {
        flexBasis(100.percent)
        padding(16.px)
        paddingTop(0.px)
        textAlign("justify")
        fontSize(13.pt)
    }

    val creditsLine by style {
        flexBasis(100.percent)
        padding(16.px)
        paddingTop(0.px)
        paddingBottom(0.px)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
    }

    val socialMediaIcon by style {
        marginTop(2.px)
        marginLeft(10.px)
    }

    val achievementContainer by style {
        display(DisplayStyle.Flex)
        margin(8.px)
        flexDirection(FlexDirection.Column)
        width(96.px)
    }

    val achievementIconContainer by style {
        width(100.percent)
        height(96.px)
        borderRadius(9.px)
        display(DisplayStyle.Flex)
    }

    val achievementIcon by style {
        width(100.percent)
        height(100.percent)
        borderRadius(8.px)
    }

    val achievementCaption by style {
        width(100.percent)
        paddingTop(8.px)
        textAlign("center")
        boxSizing("border-box")
        property("word-wrap", "break-word")
        fontSize(10.pt)
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
//        animation(swayAnimation) {
//            duration(3.s)
//            timingFunction(AnimationTimingFunction.EaseInOut)
//            iterationCount(Int.MAX_VALUE)
//            direction(AnimationDirection.Alternate)
//        }
//
//        animation(bobAnimation) {
//            duration(1.1.s)
//            timingFunction(AnimationTimingFunction.EaseInOut)
//            iterationCount(Int.MAX_VALUE)
//            direction(AnimationDirection.Alternate)
//        }
    }

//    val swayAnimation by keyframes {
//        from {
//            marginLeft((-1).percent)
//        }
//
//        to {
//            marginTop(1.percent)
//        }
//    }
//
//    val bobAnimation by keyframes {
//        from {
//            marginTop((-0.3).percent)
//        }
//
//        to {
//            marginTop(0.3.percent)
//        }
//    }

    val chatContainer by style {
        width(100.percent)
        height(100.percent)
        maxHeight(65.vh)
        flexShrink(1)
        position(Position.Relative)
    }

    val ttsMutedSettingContainer by style {
        position(Position.Absolute)
        top((-96).px)
        right(50.px)
        width(96.px)
        height(96.px)
        cursor("pointer")
    }

    val ttsMutedSettingIcon by style {
        position(Position.Absolute)
        width(96.px)
        height(96.px)
    }

    val achievementCompleteTrophyIcon by style {
        position(Position.Absolute)
        top((-96).px)
        left(40.px)
        width(96.px)
        height(96.px)
        cursor("pointer")
    }

    val chatBackground by style {
        position(Position.Absolute)
        left(20.px)
        right(20.px)
        top(20.px)
        bottom(20.px)
        backgroundColor(Color.black)
        property("z-index", -1)
    }

    val chatBorderContainer by style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Grid)
        gridTemplateColumns("75px auto 75px")
        gridTemplateRows("75px auto 75px")
    }

    val chatMessageContainer by style {
        width(100.percent)
        height(100.percent)
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        overflow("scroll")
        gridColumn("2")
        gridRow("2")
        lineHeight("1.3")
        property("scrollbar-width", "none")
    }

    val chatBorderCorner by style {
        width(100.percent)
    }

    val chatMessage by style {
        fontSize(16.px)
        property("word-wrap", "break-word")
        paddingBottom(32.px)
    }

    val chatMessageConsoleUser by style {
        color(Color.lime)
    }

    val chatMessageConsoleMarkov by style {
        color(rgb(255, 40, 20))
    }

    val chatMessageConsoleLocation by style {
        color(Color.dodgerblue)
    }

    val chatMessageConsoleUnimportant by style {
        color(Color.lightgray)
    }

    val chatMessageError by style {
        color(rgb(0xcc, 0x00, 0x00))
    }

    val chatInputContainer by style {
        width(100.percent)
        position(Position.Relative)
        paddingLeft(8.px)
        paddingRight(8.px)
        marginTop(8.px)
        boxSizing("border-box")
    }

    val chatInputBackground by style {
        position(Position.Absolute)
        left(10.px)
        right(10.px)
        top(10.px)
        bottom(10.px)
        backgroundColor(Color.black)
        property("z-index", -1)
    }

    val chatInput by style {
        width(100.percent)
        backgroundColor(Color.transparent)
        property("border", "none")
        fontSize(16.px)
        paddingTop(10.px)
        paddingBottom(10.px)
        gridColumn("2")
        gridRow("2")
        boxSizing("border-box")
        fontFamily("Consolas", "monospace")
        color(Color.white)
    }

    val chatInputBorderContainer by style {
        width(100.percent)
        height(100.percent)
    }
}