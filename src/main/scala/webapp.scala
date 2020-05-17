import java.time.{LocalDate, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import org.scalajs.dom
import org.scalajs.dom.{Element, Event, KeyboardEvent, document, html}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object webapp {
  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!")
  }

  var mvfact = 1

  @JSExportTopLevel("mvfactChanged")
  def mvfactChanged(v: String): Unit = {
    val vi = v.toInt
    document.getElementById("mvfacttxt").textContent = s"mvfact: $vi"
    mvfact = vi
  }

  def slider(name: String, range: (Int, Int)): Element = {
    val input = document.createElement("input")
    input.setAttribute("id", s"${name}_slider")
    input.setAttribute("type", "range")
    input.setAttribute("class", "slider")
    input.setAttribute("min", range._1.toString)
    input.setAttribute("max", range._2.toString)

    input.addEventListener("oninput", { e: Event =>
      println(e.toString)
    })

    val output = document.createElement("output")
    output.setAttribute("id", s"${name}_output")
    output.innerHTML = name

    val div = document.createElement("div")
    div.appendChild(output)
    div.appendChild(input)

    div
  }

  def notmain(args: Array[String]): Unit = {
    println(s"--- loaded @ ${LocalDateTime.now(ZoneOffset.UTC)} UTC ---")
    println(s"args: ${args.mkString(",")}")

    val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
    val c = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight


    document.body.appendChild(slider("slidertest", (1, 100)))


//    val ccontainer = document.getElementById("canvas_container")
//    ccontainer.setAttribute(
//      "style",
//      s"width:${dom.window.innerWidth}, height:${dom.window.innerHeight}," +
//        "padding: 0px; margin: 0px; border:1px solid red"
//    )


//    canvas.width = 600
//    canvas.height = 800

    //dom.window.onresize
    c.fillStyle = "#fAfAfA"
    c.fillRect(0, 0, canvas.width, canvas.height)



    println(c)
    println(s"canvas.width: ${canvas.width}, canvas.height: ${canvas.height}")

    c.fillStyle = "black"
    var down = false
    canvas.onmousedown = (e: dom.MouseEvent) => down = true
    canvas.onmouseup = (e: dom.MouseEvent) => down = false

    canvas.onmousemove = {
      (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()
        println(rect.left, rect.top, e.clientX, e.clientY)
        if (down) c.fillRect(
          e.clientX - rect.left,
          e.clientY - rect.top,
          10, 10
        )
    }

    // TODO difference?
//    canvas.onkeydown
//    dom.window.onkeydown



    class GameLoop {
      var tsLast: Double = 0
      var fpsCalcLast: Double = 0

      val calcFpsInterval = Duration(100, TimeUnit.MILLISECONDS)

      var frameTimes = mutable.ArrayBuffer.empty[Double]
      val FpsLimit = 60
      frameTimes.sizeHint((calcFpsInterval.toSeconds * FpsLimit).toInt)

      var avgFrameTimeToDispaly: Double = 0
      var fpsToDisplay: Double = 0

      dom.document.onkeydown = (e: dom.KeyboardEvent) => {
        println("down", e.key)
        e.key.toLowerCase match {
          case "escape" => player.reset()
          case _ => ()
        }
      }

      dom.document.onkeypress = (e: dom.KeyboardEvent) => {
        println("press", e.key)
        e.key.toLowerCase match {
          case "w" => player.move(( 0, -1))
          case "a" => player.move((-1,  0))
          case "s" => player.move(( 0, +1))
          case "d" => player.move((+1,  0))

          case _ => ()
        }
      }

      dom.document.onkeyup = (e: dom.KeyboardEvent) => println("up", e.key)

      class Player {
        var pos: (Int, Int) = _
        var dims: (Int, Int) = _
        var color: String = _

        def reset(): Unit = {
          pos = (20, 20)
          dims = (10, 10)
          color = "#FF00FF"
        }

        def move(delta: (Int, Int)): Unit = {
          val newpos = (pos._1 + mvfact * delta._1, pos._2 + mvfact * delta._2)
          println("newpos == ", newpos)

          // need to make sure the whole box is withing bounds
          if (!(newpos._1 < 0 || newpos._1 + dims._1 > canvas.width
            || newpos._2 < 0 || newpos._2 + dims._2 > canvas.height)) {
            pos = newpos
          }
        }

        def render(ts: Double): Unit = {
          val savedstyle = c.fillStyle
          c.fillStyle = color
          c.fillRect(pos._1, pos._2, dims._1, dims._2)
          c.fillStyle = savedstyle
        }

        reset()
      }

      val player = new Player

      def render(ts: Double): Unit = {
        frameTimes.append(ts - tsLast)
        tsLast = ts

        if (ts - fpsCalcLast > calcFpsInterval.toMillis) {
          avgFrameTimeToDispaly = frameTimes.sum / frameTimes.size
          fpsToDisplay = frameTimes.size / ((ts - fpsCalcLast) / 1000.0)
          fpsCalcLast = ts
          frameTimes.clear
        }

        c.clearRect(0, 0, canvas.width, canvas.height)

        // bg color
        c.fillStyle = "#f8f8f8"
        c.fillRect(0, 0, canvas.width, canvas.height)

        // bg wireframe
        val margin = 10
        c.fillStyle = "#000000"
        c.strokeRect(margin, margin, canvas.width * 0.6 - margin, canvas.height - margin * 2)

        player.render(ts)

        val fontHeight= 12
        c.font = s"${fontHeight}px Source Code Pro"
        c.fillStyle = "#000000"
        c.fillText(s"fps: $fpsToDisplay", canvas.width - 100, fontHeight)

        tsLast = ts
        dom.window.requestAnimationFrame(ts => render(ts))
      }
    }

    /*code*/

    (new GameLoop).render(dom.window.performance.now())
  }
}
