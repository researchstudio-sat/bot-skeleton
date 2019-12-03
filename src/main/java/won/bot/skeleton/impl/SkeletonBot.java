package won.bot.skeleton.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.LogAction;
import won.bot.framework.eventbot.action.impl.RandomDelayedAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.lifecycle.ActEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.OpenFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.AtomUriInNamedListFilter;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.BaseEventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.skeleton.action.ConnectWithAssociatedAtomAction;
import won.bot.skeleton.action.CreateEchoAtomWithSocketsAction;
import won.bot.skeleton.action.RespondWithEchoToMessageAction;
import won.protocol.model.SocketType;


import won.bot.framework.extensions.matcher.MatcherBehaviour;
import won.bot.framework.extensions.matcher.MatcherExtension;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;

public class SkeletonBot extends EventBot implements MatcherExtension{

    private int registrationMatcherRetryInterval;
    private MatcherBehaviour matcherBehaviour;

    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }

    @Override
    public MatcherBehaviour getMatcherBehaviour() {
        return matcherBehaviour;
    }

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        EventBus bus = getEventBus();

        // set up matching extension
        // as this is an extension, it can be activated and deactivated as needed
        // if activated, a MatcherExtensionAtomCreatedEvent is sent every time a new atom is created on a monitored node
        matcherBehaviour = new MatcherBehaviour(ctx, "BotSkeletonMatchingExtension", registrationMatcherRetryInterval);
        matcherBehaviour.activate();

        // create filters to determine which atoms the bot should react to
        NotFilter noOwnAtoms = new NotFilter(new AtomUriInNamedListFilter(ctx, ctx.getBotContextWrapper().getAtomCreateListName()));
        // TODO: noDebugAtoms or noBotAtoms to prevent reacting to other bot atoms
        // other filter possibilities: noPersonas, noReactionAtoms, onlyJobSearchAtoms,...

        // create the echo atom - if we're not reacting to the creation of our own echo
        // atom.
        BaseEventListener atomCreator = new ActionOnEventListener(
                ctx, noOwnAtoms, new CreateEchoAtomWithSocketsAction(ctx)
        );
        // listen for the MatcherExtensionAtomCreatedEvent
        bus.subscribe(MatcherExtensionAtomCreatedEvent.class, atomCreator);
        // as soon as the echo atom is created, connect to original
        BaseEventListener atomConnector = new ActionOnEventListener(ctx, "atomConnector",
                                                                    new RandomDelayedAction(
                                                                            ctx,
                                                                            5000,
                                                                            5000,
                                                                            1,
                                                                            new ConnectWithAssociatedAtomAction(ctx,
                                                                                                                SocketType.ChatSocket
                                                                                                                        .getURI(),
                                                                                                                SocketType.ChatSocket
                                                                                                                        .getURI(),
                                                                                                                "Greetings! I am the EchoBot! I will repeat everything you say, which you might "
                                                                                                                        + "find useful for testing purposes."
                                                                            )
                                                                    )
        );
        bus.subscribe(AtomCreatedEvent.class, atomConnector);

        // add a listener that auto-responds to messages by a message
        // after 10 messages, it unsubscribes from all events
        // subscribe it to:
        // * message events - so it responds
        // * open events - so it initiates the chain reaction of responses
        BaseEventListener autoResponder = new ActionOnEventListener(ctx, new RespondWithEchoToMessageAction(ctx));

        bus.subscribe(OpenFromOtherAtomEvent.class, autoResponder);
        bus.subscribe(MessageFromOtherAtomEvent.class, autoResponder);
        bus.subscribe(CloseFromOtherAtomEvent.class,
                new ActionOnEventListener(ctx, new LogAction(ctx, "received close message from remote atom.")));
    }
}
