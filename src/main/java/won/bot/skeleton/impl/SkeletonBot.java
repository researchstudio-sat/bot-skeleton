package won.bot.skeleton.impl;

import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.impl.LogAction;
import won.bot.framework.eventbot.action.impl.RandomDelayedAction;
import won.bot.framework.eventbot.action.impl.matcher.RegisterMatcherAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.lifecycle.ActEvent;
import won.bot.framework.eventbot.event.impl.matcher.AtomCreatedEventForMatcher;
import won.bot.framework.eventbot.event.impl.matcher.MatcherRegisterFailedEvent;
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

public class SkeletonBot extends EventBot {

    private int registrationMatcherRetryInterval;

    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        EventBus bus = getEventBus();
        // register with WoN nodes, be notified when new atoms are created
        RegisterMatcherAction registerMatcherAction = new RegisterMatcherAction(ctx);
        BaseEventListener matcherRegistrator = new ActionOnEventListener(ctx, registerMatcherAction, 1);
        bus.subscribe(ActEvent.class, matcherRegistrator);
        RandomDelayedAction delayedRegistration = new RandomDelayedAction(ctx, registrationMatcherRetryInterval,
                registrationMatcherRetryInterval, 0, registerMatcherAction);
        ActionOnEventListener matcherRetryRegistrator = new ActionOnEventListener(ctx, delayedRegistration);
        bus.subscribe(MatcherRegisterFailedEvent.class, matcherRetryRegistrator);
        // create the echo atom - if we're not reacting to the creation of our own echo
        // atom.
        BaseEventListener atomCreator = new ActionOnEventListener(
                ctx,
                new NotFilter(new AtomUriInNamedListFilter(
                        ctx,
                        ctx.getBotContextWrapper().getAtomCreateListName()
                )),
                new CreateEchoAtomWithSocketsAction(ctx)
        );
        bus.subscribe(AtomCreatedEventForMatcher.class, atomCreator);
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
