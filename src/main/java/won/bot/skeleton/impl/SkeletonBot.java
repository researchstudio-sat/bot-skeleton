package won.bot.skeleton.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.LogAction;
import won.bot.framework.eventbot.action.impl.RandomDelayedAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.AtomUriInNamedListFilter;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.BaseEventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.extensions.serviceatom.ServiceAtomBehaviour;
import won.bot.framework.extensions.serviceatom.ServiceAtomExtension;
import won.bot.skeleton.action.ConnectWithAssociatedAtomAction;
import won.bot.skeleton.action.CreateEchoAtomWithSocketsAction;
import won.bot.skeleton.action.RespondWithEchoToMessageAction;
import won.protocol.model.SocketType;


import won.bot.framework.extensions.matcher.MatcherBehaviour;
import won.bot.framework.extensions.matcher.MatcherExtension;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;

public class SkeletonBot extends EventBot implements MatcherExtension, ServiceAtomExtension {

    private int registrationMatcherRetryInterval;
    private MatcherBehaviour matcherBehaviour;
    private ServiceAtomBehaviour serviceAtomBehaviour;

    // bean setter, used by spring
    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }

    @Override
    public ServiceAtomBehaviour getServiceAtomBehaviour() {
        return serviceAtomBehaviour;
    }

    @Override
    public MatcherBehaviour getMatcherBehaviour() {
        return matcherBehaviour;
    }

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        EventBus bus = getEventBus();

        // activate ServiceAtomBehaviour
        serviceAtomBehaviour = new ServiceAtomBehaviour(ctx);
        serviceAtomBehaviour.activate();

        // set up matching extension
        // as this is an extension, it can be activated and deactivated as needed
        // if activated, a MatcherExtensionAtomCreatedEvent is sent every time a new atom is created on a monitored node
        matcherBehaviour = new MatcherBehaviour(ctx, "BotSkeletonMatchingExtension", registrationMatcherRetryInterval);
        matcherBehaviour.activate();

        // create filters to determine which atoms the bot should react to
        NotFilter noOwnAtoms = new NotFilter(new AtomUriInNamedListFilter(ctx, ctx.getBotContextWrapper().getAtomCreateListName()));
        // filter to prevent reacting to serviceAtom<->ownedAtom events;
        NotFilter noInternalServiceAtomEventFilter = getNoInternalServiceAtomEventFilter();
        // TODO: noDebugAtoms or noBotAtoms to prevent reacting to other bot atoms
        // noInternalServiceAtomEventFilter
        // other filter possibilities: noPersonas, noReactionAtoms, onlyJobSearchAtoms,...

        // create the echo atom - if we're not reacting to the creation of our own echo
        // atom.
        BaseEventListener atomCreator = new ActionOnEventListener(
                ctx, noOwnAtoms, new CreateEchoAtomWithSocketsAction(ctx)
        );
        // listen for the MatcherExtensionAtomCreatedEvent
        bus.subscribe(MatcherExtensionAtomCreatedEvent.class, atomCreator);
        // as soon as the echo atom is created, connect to original
        bus.subscribe(AtomCreatedEvent.class, noInternalServiceAtomEventFilter, new RandomDelayedAction(
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
                ))
        );

        // add a listener that auto-responds to messages by a message
        // after 10 messages, it unsubscribes from all events
        // subscribe it to:
        // * message events - so it responds
        // * open events - so it initiates the chain reaction of responses
        bus.subscribe(ConnectFromOtherAtomEvent.class, noInternalServiceAtomEventFilter, new RespondWithEchoToMessageAction(ctx));
        bus.subscribe(MessageFromOtherAtomEvent.class, noInternalServiceAtomEventFilter, new RespondWithEchoToMessageAction(ctx));
        bus.subscribe(CloseFromOtherAtomEvent.class,
                new ActionOnEventListener(ctx, new LogAction(ctx, "received close message from remote atom.")));
    }
}
