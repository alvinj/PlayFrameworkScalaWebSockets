package controllers

import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.SimpleWebSocketActor
import play.api.libs.json._

// the ‘implicit’ here is needed for the `ActorFlow.actorRef` below.
// i thought a Materializer was needed, but it is not.
// class WebSocketsController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
class WebSocketsController @Inject() (cc: ControllerComponents)(implicit system: ActorSystem)
extends AbstractController(cc)
{
    val logger = play.api.Logger(getClass)

    // this method displays the index.scala.html page/template
    def index = Action { implicit request: Request[AnyContent] =>
        logger.info("index page was called")
        Ok(views.html.index())
    }

    // our WebSocket. DOCS on WebSocket.accept:
    // def accept[In, Out](f: RequestHeader => Flow[In, Out, _])(implicit transformer: WebSocket.MessageFlowTransformer[In,Out]): WebSocket
    // Accepts a WebSocket using the given flow.
    def ws = WebSocket.accept[JsValue, JsValue] { requestHeader =>
        logger.info("'ws' function is called")
        // DOCS: “Play’s WebSocket handling mechanism is built around Akka streams. 
        // A WebSocket is modeled as a Flow, incoming WebSocket messages are 
        // fed into the flow, and messages produced by the flow are sent out 
        // to the client.”
        //
        // “To handle a WebSocket with an actor, we can use a Play utility, ActorFlow, 
        // to convert an ActorRef to a Flow. This utility takes a function that 
        // converts the ActorRef to send messages to a akka.actor.Props object 
        // that describes the actor that Play should create when it receives 
        // the WebSocket connection.”
        //
        // “Any messages received from the client will be sent to the actor, 
        // and any messages sent to the actor supplied by Play will be sent 
        // to the client.”
        // 
        // `ActorFlow.actorRef` DOCS: Create a flow that is handled by an actor.
        // Messages can be sent downstream by sending them to the actor passed 
        // into the props function.
        // def actorRef[In, Out](props: ActorRef => Props ...)
        //     (implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _]
        ActorFlow.actorRef { actorRef =>
            logger.info("ws: calling My Actor")
            SimpleWebSocketActor.props(actorRef)
        }
    }

}



