/*
 * Copyright 2012 Research Studios Austria Forschungsges.m.b.H. Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package won.bot.skeleton.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.EventBotActionUtils;
import won.bot.framework.eventbot.action.impl.atomlifecycle.AbstractCreateAtomAction;
import won.bot.framework.eventbot.event.AtomCreationFailedEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.FailureResponseEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.protocol.message.WonMessage;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;

import java.lang.invoke.MethodHandles;
import java.net.URI;

/**
 * Creates an atom with the specified sockets. If no socket is specified, the
 * chatSocket will be used.
 */
public class CreateEchoAtomWithSocketsAction extends AbstractCreateAtomAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CreateEchoAtomWithSocketsAction(EventListenerContext eventListenerContext, URI... sockets) {
        super(eventListenerContext, sockets);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();

        // check event type
        if (!(event instanceof MatcherExtensionAtomCreatedEvent)) {
            logger.error("CreateEchoAtomWithSocketsAction can only handle MatcherExtensionAtomCreatedEvent");
            return;
        }

        // get needed information
        WonNodeInformationService wonNodeInformationService = ctx.getWonNodeInformationService();
        final URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        final URI atomURI = wonNodeInformationService.generateAtomURI(wonNodeUri);
        URI reactingToAtomUri = ((MatcherExtensionAtomCreatedEvent) event).getAtomURI();

        Dataset atomDataset = ((MatcherExtensionAtomCreatedEvent) event).getAtomData();
        DefaultAtomModelWrapper atomModelWrapper = atomDataset != null ? new DefaultAtomModelWrapper(atomDataset) : new DefaultAtomModelWrapper(atomURI);


        // determine and set information of new atom
        String titleString = atomModelWrapper.getSomeTitleFromIsOrAll("en", "de");
        String atomTitle = titleString != null ? titleString : "Your Posting (" + reactingToAtomUri.toString() + ")";
        atomModelWrapper = new DefaultAtomModelWrapper(atomURI);
        atomModelWrapper.setTitle("RE: " + atomTitle);
        atomModelWrapper.setDescription("This is an atom automatically created by the EchoBot.");
//        atomModelWrapper.setSeeksTitle("RE: " + atomTitle);
//        atomModelWrapper.setSeeksDescription("This is an atom automatically created by the EchoBot.");

        int i = 1;
        for (URI socket : sockets) {
            atomModelWrapper.addSocket(atomURI.toString() + "#socket" + i, socket.toString());
            i++;
        }

        final Dataset echoAtomDataset = atomModelWrapper.copyDataset();
        WonMessage createAtomMessage = createWonMessage(atomURI, wonNodeUri, echoAtomDataset);
        logger.debug("creating atom on won node {} with content {} ", wonNodeUri,
                        StringUtils.abbreviate(RdfUtils.toString(echoAtomDataset), 150));


        // remember the atom URI so we can react to success/failure responses
        EventBotActionUtils.rememberInList(ctx, atomURI, uriListName);
        EventListener successCallback = event12 -> {
            logger.debug("atom creation successful, new atom URI is {}", atomURI);
            // save the mapping between the original and the reaction in to the context.
            ctx.getBotContextWrapper().addUriAssociation(reactingToAtomUri, atomURI);
            ctx.getEventBus().publish(new AtomCreatedEvent(atomURI, wonNodeUri, echoAtomDataset, null));
        };
        EventListener failureCallback = event1 -> {
            String textMessage = WonRdfUtils.MessageUtils
                            .getTextMessage(((FailureResponseEvent) event1).getFailureMessage());
            logger.debug("atom creation failed for atom URI {}, original message URI {}: {}", new Object[] {
                            atomURI, ((FailureResponseEvent) event1).getOriginalMessageURI(), textMessage });
            EventBotActionUtils.removeFromList(ctx, atomURI, uriListName);
            ctx.getEventBus().publish(new AtomCreationFailedEvent(wonNodeUri));
        };
        EventBotActionUtils.makeAndSubscribeResponseListener(createAtomMessage, successCallback, failureCallback, ctx);
        logger.debug("registered listeners for response to message URI {}", createAtomMessage.getMessageURI());
        getEventListenerContext().getWonMessageSender().sendMessage(createAtomMessage);
        logger.debug("atom creation message sent with message URI {}", createAtomMessage.getMessageURI());
    }
}
