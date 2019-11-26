package fi.utu.protproc.group3.finitestatemachine;

import fi.utu.protproc.group3.protocols.bgp4.BGP4MessageNotification;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class FSMImpl {
    enum FSMEvent {
        ManualStart, ManualStop, AutomaticStart, ManualStart_with_PassiveTcpEstablishment,
        AutomaticStart_with_PassiveTcpEstablishment, AutomaticStart_with_DampPeerOscillations,
        AutomaticStart_with_DampPeerOscillations_and_PassiveTcpEstablishment,
        AutomaticStop, ConnectRetryTimer_Expires, HoldTimer_Expires, KeepaliveTimer_Expires,
        DelayOpenTimer_Expires, IdleHoldTimer_Expires, TcpConnection_Valid, Tcp_CR_Invalid,
        Tcp_CR_Acked, TcpConnectionConfirmed, TcpConnectionFails, BGPOpen,
        BGPOpen_with_DelayOpenTimer_running, BGPHeaderErr, BGPOpenMsgErr, OpenCollisionDump,
        NotifMsgVerErr, NotifMsg, KeepAliveMsg, UpdateMsg, UpdateMsgErr
    };

    public UntypedStateMachineBuilder builder;

    private final static FSMImpl builderInstance = new FSMImpl();
    public static UntypedStateMachine newInstance(InternalFSMCallbacks callbacks) {
        return builderInstance.builder.newStateMachine("IDLE", callbacks);
    }

    private FSMImpl() {
        this.builder = StateMachineBuilderFactory.create(StateMachineSample.class, new Class<?>[] { InternalFSMCallbacks.class });

        // IDLE
        {
            builder.externalTransition().from("IDLE").to("CONNECT").on(FSMEvent.ManualStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from("IDLE").to("CONNECT").on(FSMEvent.AutomaticStart).callMethod("onManualStart_AutomaticStartIdle");
            builder.externalTransition().from("IDLE").to("ACTIVE").on(FSMEvent.ManualStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
            builder.externalTransition().from("IDLE").to("ACTIVE").on(FSMEvent.AutomaticStart_with_PassiveTcpEstablishment).callMethod("onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle");
        }

        // CONNECT
        {
            builder.onEntry("CONNECT").callMethod("onConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopConnect");
            builder.internalTransition().within("CONNECT").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresConnect");
            builder.externalTransition().from("CONNECT").to("OPEN_SENT").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresConnect");
            builder.externalTransition().from("CONNECT").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.BGPOpen).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventConnect");
            builder.externalTransition().from("CONNECT").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventConnect");
        }

        // ACTIVE}
        {
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopActive");
            builder.externalTransition().from("ACTIVE").to("CONNECT").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onConnectRetryTimer_ExpiresActive");
            builder.externalTransition().from("ACTIVE").to("OPEN_SENT").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onDelayOpenTimer_ExpiresActive");
            builder.internalTransition().within("ACTIVE").on(FSMEvent.TcpConnection_Valid).callMethod("onTcpConnection_ValidActive");
            builder.internalTransition().within("ACTIVE").on(FSMEvent.Tcp_CR_Invalid).callMethod("onTcp_CR_InvalidActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFailsActive");
            builder.externalTransition().from("ACTIVE").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onBGPOpen_with_DelayOpenTimer_runningActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.BGPOpen).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventActive");
            builder.externalTransition().from("ACTIVE").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventActive");
        }

        // OPEN_SENT
        {
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("ACTIVE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFailsOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("OPEN_CONFIRM").on(FSMEvent.BGPOpen).callMethod("onBGPOpenOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.KeepAliveMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventOpenSent");
            builder.externalTransition().from("OPEN_SENT").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventOpenSent");
        }

        // OPEN_CONFIRM
        {
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresOpenConfirm");
            builder.internalTransition().within("OPEN_CONFIRM").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onNotifMsgVerErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onBGPHeaderErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onBGPOpenMsgErrOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("ESTABLISHED").on(FSMEvent.KeepAliveMsg).callMethod("onKeepAliveMsgOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onAnyOtherEventOpenConfirm");
            builder.externalTransition().from("OPEN_CONFIRM").to("IDLE").on(FSMEvent.UpdateMsgErr).callMethod("onAnyOtherEventOpenConfirm");
        }

        // ESTABLISHED
        {
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.ManualStop).callMethod("onManualStopEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.AutomaticStop).callMethod("onAutomaticStopEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.HoldTimer_Expires).callMethod("onHoldTimer_ExpiresEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.KeepaliveTimer_Expires).callMethod("onKeepaliveTimer_ExpiresEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.OpenCollisionDump).callMethod("onOpenCollisionDumpEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.TcpConnectionFails).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.NotifMsgVerErr).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.NotifMsg).callMethod("onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.KeepAliveMsg).callMethod("onKeepAliveMsgEstablished");
            builder.internalTransition().within("ESTABLISHED").on(FSMEvent.UpdateMsg).callMethod("onUpdateMsgEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.UpdateMsg).callMethod("onUpdateMsgErrEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.ConnectRetryTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.DelayOpenTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.IdleHoldTimer_Expires).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPOpen_with_DelayOpenTimer_running).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPHeaderErr).callMethod("onAnyOtherEventEstablished");
            builder.externalTransition().from("ESTABLISHED").to("IDLE").on(FSMEvent.BGPOpenMsgErr).callMethod("onAnyOtherEventEstablished");
        }
    }


    @StateMachineParameters(stateType=String.class, eventType=FSMEvent.class, contextType=Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
        static int connectRetryCounter = 0;
        static int connectRetryTime = 120;
        static int holdTime = 90;
        static int keepaliveTime = 30;

        Timer connectRetryTimer = new Timer();
        Timer holdTimer = new Timer();
        Timer keepaliveTimer = new Timer();

        // Optional attributes
        static int delayOpenTime = 120; // Not sure
        Timer delayOpenTimer = new Timer();
        static boolean dampPeerOscillations;
        static boolean sendNOTIFICATIONwithoutOPEN;

        private final InternalFSMCallbacks callbacks;

        public StateMachineSample(InternalFSMCallbacks callbacks) {
            Objects.requireNonNull(callbacks);

            this.callbacks = callbacks;
        }

        TimerTask connectRetryTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (connectRetryTime > 0) {
                    System.out.println("Seconds = " + connectRetryTime);
                    connectRetryTime--;
                } else {
                    // stop the timer
                    cancel();
                }
            }
        };

        TimerTask delayOpenTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (delayOpenTime > 0) {
                    System.out.println("Seconds = " + delayOpenTime);
                    delayOpenTime--;
                } else {
                    // stop the timer
                    cancel();
                }
            }
        };

        TimerTask holdTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (holdTime > 0) {
                    System.out.println("Seconds = " + holdTime);
                    holdTime--;
                } else {
                    // stop the timer
                    cancel();
                }
            }
        };

        TimerTask keepaliveTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (keepaliveTime > 0) {
                    System.out.println("Seconds = " + keepaliveTime);
                    keepaliveTime--;
                } else {
                    // stop the timer
                    cancel();
                }
            }
        };

        // IDLE -------------------------------------------------------------------------------------------------------
        protected void onManualStart_AutomaticStartIdle(String from, String to, FSMEvent event, Integer context) {
            // initializes all BGP resources for the peer connection
            callbacks.initializeBGPResources();

            // sets ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // starts the ConnectRetryTimer with the initial value
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // initiates a TCP connection to the other BGP peer
            callbacks.connectRemotePeer();

            // listens for a connection that may be initiated by the remote BGP peer
            callbacks.listenForTCPConnection();

            // changes its state to Connect
            System.out.println("Changing state from IDLE to CONNECT");
        }

        protected void onManualStart_with_PassiveTcpEstablishment_AutomaticStart_with_PassiveTcpEstablishmentIdle(String from, String to, FSMEvent event, Integer context) {
            // initializes all BGP resources
            callbacks.initializeBGPResources();

            // sets the ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // starts the ConnectRetryTimer with the initial value
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // listens for a connection that may be initiated by the remote peer
            callbacks.listenForTCPConnection();

            // changes its state to Active
            System.out.println("Changing state from IDLE to ACTIVE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // CONNECT ----------------------------------------------------------------------------------------------------
        protected void onManualStopConnect(String from, String to, FSMEvent event, Integer context) {
            // drops the TCP connection
            callbacks.dropTCPConnection();

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // sets ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // stops the ConnectRetryTimer and sets ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // Change the state to IDLE
            System.out.println("Changing state from CONNECT to IDLE");
        }

        protected void onConnectRetryTimer_ExpiresConnect(String from, String to, FSMEvent event, Integer context) {
            // drops the TCP connection
            callbacks.dropTCPConnection();

            // restarts the ConnectRetryTimer
            connectRetryTimer.cancel();
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // stops the DelayOpenTimer and resets the timer to zero
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // initiates a TCP connection to the other BGP peer
            callbacks.connectRemotePeer();

            // continues to listen for a connection that may be initiated by the remote BGP peer
            callbacks.listenForTCPConnection();

            // stays in the Connect state
            System.out.println("Remaining in CONNECT state");
        }

        protected void onDelayOpenTimer_ExpiresConnect(String from, String to, FSMEvent event, Integer context) {
            // sends an OPEN message to its peer
            callbacks.sendOpenMessage();

            // sets the HoldTimer to a large value
            holdTimer.cancel();
            holdTime = 240;
            holdTimer.schedule(holdTimerTask, 0, 1000);

            // changes its state to OpenSent
            System.out.println("Changing state from CONNECT to OPEN_SENT");
        }

        // TODO: If the TCP connection succeeds (Event 16 or Event 17), the local
        //      system checks the DelayOpen attribute prior to processing.  If the
        //      DelayOpen attribute is set to TRUE, the local system:
        //        - stops the ConnectRetryTimer (if running) and sets the
        //          ConnectRetryTimer to zero,
        //        - sets the DelayOpenTimer to the initial value, and
        //        - stays in the Connect state.
        //        If the DelayOpen attribute is set to FALSE, the local system:
        //        - stops the ConnectRetryTimer (if running) and sets the
        //          ConnectRetryTimer to zero,
        //        - completes BGP initialization
        //        - sends an OPEN message to its peer,
        //        - sets the HoldTimer to a large value, and
        //        - changes its state to OpenSent.


        // TODO: If the TCP connection fails (Event 18), the local system checks
        //      the DelayOpenTimer.  If the DelayOpenTimer is running, the local
        //      system:
        //        - restarts the ConnectRetryTimer with the initial value,
        //        - stops the DelayOpenTimer and resets its value to zero,
        //        - continues to listen for a connection that may be initiated by
        //          the remote BGP peer, and
        //        - changes its state to Active.
        //      If the DelayOpenTimer is not running, the local system:
        //      - stops the ConnectRetryTimer to zero,
        //        - drops the TCP connection,
        //        - releases all BGP resources, and
        //        - changes its state to Idle.

        protected void onBGPOpen_with_DelayOpenTimer_runningConnect(String from, String to, FSMEvent event, Integer context) {
            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            if(connectRetryTimerTask.scheduledExecutionTime() > 0) {
                connectRetryTimer.cancel();
                connectRetryTime = 0;
            }

            // completes the BGP initialization
            callbacks.completeBGPPeerInitialization();

            // stops and clears the DelayOpenTimer (sets the value to zero)
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // sends an OPEN message
            callbacks.sendOpenMessage();

            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            if(holdTimerTask.scheduledExecutionTime() > 0 && holdTime != 0) {
                // starts the KeepaliveTimer with the initial value
                keepaliveTime = 30;
                keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);

                // resets the HoldTimer to the negotiated value
                holdTimer.cancel();
                holdTime = 90;
                holdTimer.schedule(holdTimerTask, 0, 1000);
            } else {
                // resets the KeepaliveTimer
                keepaliveTimer.cancel();
                keepaliveTime = 0;

                // resets the HoldTimer value to zero
                holdTimer.cancel();
                holdTime = 0;
            }

            // and changes its state to OpenConfirm
            System.out.println("Changing state from CONNECT to OPEN_CONFIRM");
        }

        protected void onBGPHeaderErrConnect(String from, String to, FSMEvent event, Integer context) {
            // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
            //          set to TRUE, then the local system first sends a NOTIFICATION
            //          message with the appropriate error code
            if(sendNOTIFICATIONwithoutOPEN) {
                callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR);
            }

            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            if(connectRetryTimerTask.scheduledExecutionTime() > 0 && connectRetryTime > 0) {
                connectRetryTimer.cancel();
                connectRetryTime = 0;
            }

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from CONNECT to IDLE");
        }

        protected void onBGPOpenMsgErrConnect(String from, String to, FSMEvent event, Integer context) {
            // (optionally) If the SendNOTIFICATIONwithoutOPEN attribute is
            //          set to TRUE, then the local system first sends a NOTIFICATION
            //          message with the appropriate error code
            if(sendNOTIFICATIONwithoutOPEN) {
                callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR);
            }

            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            if(connectRetryTimerTask.scheduledExecutionTime() > 0 && connectRetryTime > 0) {
                connectRetryTimer.cancel();
                connectRetryTime = 0;
            }

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from CONNECT to IDLE");
        }

        protected void onNotifMsgVerErrConnect(String from, String to, FSMEvent event, Integer context) {
            if(delayOpenTimerTask.scheduledExecutionTime() > 0 && delayOpenTime > 0) {
                // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
                if(connectRetryTimerTask.scheduledExecutionTime() > 0 && connectRetryTime > 0) {
                    connectRetryTimer.cancel();
                    connectRetryTime = 0;
                }

                // stops and resets the DelayOpenTimer (sets to zero)
                delayOpenTimer.cancel();
                delayOpenTime = 0;

                // releases all BGP resources
                callbacks.releaseBGPResources();

                // drops the TCP connection
                callbacks.dropTCPConnection();

                // changes its state to Idle
                System.out.println("Changing state from CONNECT to IDLE");
            } else {
                // stops the ConnectRetryTimer and sets the ConnectRetryTimer to zero
                connectRetryTimer.cancel();
                connectRetryTime = 0;

                // releases all BGP resources
                callbacks.releaseBGPResources();

                // drops the TCP connection
                callbacks.dropTCPConnection();

                // increments the ConnectRetryCounter by 1
                connectRetryCounter++;

                // performs peer oscillation damping if the DampPeerOscillations attribute is set to True
                if(dampPeerOscillations) {
                    callbacks.performPeerOscillationDamping();
                }

                // changes its state to Idle
                System.out.println("Changing state from CONNECT to IDLE");
            }
        }

        protected void onAnyOtherEventConnect(String from, String to, FSMEvent event, Integer context) {
            // if the ConnectRetryTimer is running, stops and resets the ConnectRetryTimer (sets to zero)
            if(connectRetryTimerTask.scheduledExecutionTime() > 0 && connectRetryTime > 0) {
                connectRetryTimer.cancel();
                connectRetryTime = 0;
            }

            // if the DelayOpenTimer is running, stops and resets the DelayOpenTimer (sets to zero)
            if(delayOpenTimerTask.scheduledExecutionTime() > 0 && delayOpenTime > 0) {
                delayOpenTimer.cancel();
                delayOpenTime = 0;
            }

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // performs peer oscillation damping if the DampPeerOscillations attribute is set to True
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from CONNECT to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // ACTIVE -----------------------------------------------------------------------------------------------------
        protected void onManualStopActive(String from, String to, FSMEvent event, Integer context) {
            // If the DelayOpenTimer is running and the
            // SendNOTIFICATIONwithoutOPEN session attribute is set, the
            // local system sends a NOTIFICATION with a Cease
            if(delayOpenTimerTask.scheduledExecutionTime() > 0 && delayOpenTime > 0 && sendNOTIFICATIONwithoutOPEN) {
                callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);
            }

            // releases all BGP resources including stopping the DelayOpenTimer
            callbacks.releaseBGPResources();
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // sets ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // stops the ConnectRetryTimer and sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // changes its state to Idle
            System.out.println("Changing state from ACTIVE to IDLE");
        }

        protected void onConnectRetryTimer_ExpiresActive(String from, String to, FSMEvent event, Integer context) {
            // restarts the ConnectRetryTimer (with initial value)
            connectRetryTimer.cancel();
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // initiates a TCP connection to the other BGP peer
            callbacks.connectRemotePeer();

            // continues to listen for a TCP connection that may be initiated by a remote BGP peer
            callbacks.listenForTCPConnection();

            // changes its state to Connect
            System.out.println("Changing state from ACTIVE to CONNECT");
        }

        protected void onDelayOpenTimer_ExpiresActive(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // stops and clears the DelayOpenTimer (set to zero)
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // completes the BGP initialization
            callbacks.completeBGPPeerInitialization();

            // sends the OPEN message to its remote peer
            callbacks.sendOpenMessage();

            // sets its hold timer to a large value, and
            holdTimer.cancel();
            holdTime = 240;
            holdTimer.schedule(holdTimerTask, 0, 1000);

            // changes its state to OpenSent
            System.out.println("Changing state from ACTIVE to OPEN_SENT");
        }

        protected void onTcpConnection_ValidActive(String from, String to, FSMEvent event, Integer context) {
            // the local system processes the TCP connection flags
            callbacks.processTCPConnection();

            // Remain in ACTIVE state
            System.out.println("Remaining in ACTIVE state");
        }

        protected void onTcp_CR_InvalidActive(String from, String to, FSMEvent event, Integer context) {
            // the local system rejects the TCP connection and
            callbacks.rejectTCPConnection();

            // stays in the Active State
            System.out.println("Remaining in ACTIVE state");
        }

        // TODO: In response to the success of a TCP connection (Event 16 or Event
        //      17), the local system checks the DelayOpen optional attribute
        //      prior to processing.
        //        If the DelayOpen attribute is set to TRUE, the local system:
        //          - stops the ConnectRetryTimer and sets the ConnectRetryTimer
        //            to zero,
        //          - sets the DelayOpenTimer to the initial value
        //            (DelayOpenTime), and
        //          - stays in the Active state.
        //        If the DelayOpen attribute is set to FALSE, the local system:
        //          - sets the ConnectRetryTimer to zero,
        //          - completes the BGP initialization,
        //          - sends the OPEN message to its peer,
        //          - sets its HoldTimer to a large value, and
        //          - changes its state to OpenSent.
        //      A HoldTimer value of 4 minutes is suggested as a "large value" for
        //      the HoldTimer.

        protected void onTcpConnectionFailsActive(String from, String to, FSMEvent event, Integer context) {
            // restarts the ConnectRetryTimer (with the initial value)
            connectRetryTimer.cancel();
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // stops and clears the DelayOpenTimer (sets the value to zero)
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // releases all BGP resource
            callbacks.releaseBGPResources();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // optionally performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ACTIVE to IDLE");
        }

        protected void onBGPOpen_with_DelayOpenTimer_runningActive(String from, String to, FSMEvent event, Integer context) {
            // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
            if(connectRetryTimerTask.scheduledExecutionTime() > 0) {
                connectRetryTimer.cancel();
                connectRetryTime = 0;
            }

            // stops and clears the DelayOpenTimer (sets to zero),
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // completes the BGP initialization
            callbacks.completeBGPPeerInitialization();

            // sends an OPEN message
            callbacks.sendOpenMessage();

            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            if(holdTime > 0 && holdTimerTask.scheduledExecutionTime() > 0) {
                // starts the KeepaliveTimer to initial value
                keepaliveTimer.cancel();
                keepaliveTime = 30;
                keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);

                // resets the HoldTimer to the negotiated value
                holdTime = 90;
            } else {
                // resets the KeepaliveTimer (set to zero)
                keepaliveTimer.cancel();
                keepaliveTime = 0;

                // resets the HoldTimer to zero
                holdTimer.cancel();
                holdTime = 0;
            }

            // changes its state to OpenConfirm
            System.out.println("Changing state from ACTIVE to OPEN_CONFIRM");
        }

        protected void onBGPHeaderErrActive(String from, String to, FSMEvent event, Integer context) {
            // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
            if(sendNOTIFICATIONwithoutOPEN) {
                callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR);
            }

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ACTIVE to IDLE");
        }

        protected void onBGPOpenMsgErrActive(String from, String to, FSMEvent event, Integer context) {
            // (optionally) sends a NOTIFICATION message with the appropriate error code if the SendNOTIFICATIONwithoutOPEN attribute is set to TRUE
            if(sendNOTIFICATIONwithoutOPEN) {
                callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR);
            }

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ACTIVE to IDLE");
        }

        protected void onNotifMsgVerErrActive(String from, String to, FSMEvent event, Integer context) {
            if(delayOpenTimerTask.scheduledExecutionTime() > 0 && delayOpenTime > 0) {
                // stops the ConnectRetryTimer (if running) and sets the ConnectRetryTimer to zero
                if(connectRetryTimerTask.scheduledExecutionTime() > 0) {
                    connectRetryTimer.cancel();
                    connectRetryTime = 0;
                }

                // stops and resets the DelayOpenTimer (sets to zero)
                delayOpenTimer.cancel();
                delayOpenTime = 0;

                // releases all BGP resources
                callbacks.releaseBGPResources();

                // drops the TCP connection
                callbacks.dropTCPConnection();

                // changes its state to Idle
                System.out.println("Changing state from ACTIVE to IDLE");
            } else {
                // sets the ConnectRetryTimer to zero
                connectRetryTimer.cancel();
                connectRetryTime = 0;

                // releases all BGP resources
                callbacks.releaseBGPResources();

                // drops the TCP connection
                callbacks.dropTCPConnection();

                // increments the ConnectRetryCounter by 1
                connectRetryCounter++;

                // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE{
                if(dampPeerOscillations) {
                    callbacks.performPeerOscillationDamping();
                }

                // changes its state to Idle
                System.out.println("Changing state from ACTIVE to IDLE");
            }
        }

        protected void onAnyOtherEventActive(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by one
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ACTIVE to IDLE");

        }
        // ------------------------------------------------------------------------------------------------------------

        // OPEN_SENT --------------------------------------------------------------------------------------------------
        protected void onManualStopOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // sets the ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onAutomaticStopOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all the BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onHoldTimer_ExpiresOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the error code Hold Timer Expired
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");

        }

        protected void onTcpConnectionFailsOpenSent(String from, String to, FSMEvent event, Integer context) {
            // closes the BGP connection
            callbacks.closeBGPConnection();

            // restarts the ConnectRetryTimer
            connectRetryTimer.cancel();
            connectRetryTime = 120;
            connectRetryTimer.schedule(connectRetryTimerTask, 0, 1000);

            // continues to listen for a connection that may be initiated by the remote BGP peer
            // TODO: Understand what does it mean

            // changes its state to Active
            System.out.println("Changing state from OPEN_SENT to ACTIVE");
        }

        protected void onBGPOpenOpenSent(String from, String to, FSMEvent event, Integer context) {
            // resets the DelayOpenTimer to zero
            delayOpenTimer.cancel();
            delayOpenTime = 0;

            // sets the BGP ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            // sets a KeepaliveTimer
            keepaliveTimer.cancel();
            keepaliveTime = 30;
            keepaliveTimer.schedule(keepaliveTimerTask,0, 1000);

            // sets the HoldTimer according to the negotiated value
            holdTimer.cancel();
            holdTime = 90;
            holdTimer.schedule(holdTimerTask, 0, 1000);

            // changes its state to OpenConfirm
            System.out.println("Changing state from OPEN_SENT to OPEN_CONFIRM");
        }

        protected void onBGPHeaderErrOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the appropriate error code
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onBGPOpenMsgErrOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the appropriate error code
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onOpenCollisionDumpOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onNotifMsgVerErrOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }

        protected void onAnyOtherEventOpenSent(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION with the Error Code Finite State Machine Error
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_SENT to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // CONFIRM ----------------------------------------------------------------------------------------------------
        protected void onManualStopOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION message with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // sets the ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onAutomaticStopOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION message with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onHoldTimer_ExpiresOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION message with the Error Code Hold Timer Expired
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onKeepaliveTimer_ExpiresOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            // restarts the KeepaliveTimer
            keepaliveTimer.cancel();
            keepaliveTime = 30;
            keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);

            // remains in the OpenConfirmed state
            System.out.println("Remaining in the OPEN_CONFIRM state");
        }

        protected void onTcpConnectionFails_NotifMsgOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onNotifMsgVerErrOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onBGPHeaderErrOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the appropriate error code
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_MESSAGE_HEADER_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onBGPOpenMsgErrOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the appropriate error code
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_OPEN_MESSAGE_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onOpenCollisionDumpOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }

        protected void onKeepAliveMsgOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // restarts the HoldTimer
            holdTimer.cancel();
            holdTime = 90;
            holdTimer.schedule(holdTimerTask, 0, 1000);

            // changes its state to Established
            System.out.println("Changing state from OPEN_CONFIRM to ESTABLISHED");
        }

        protected void onAnyOtherEventOpenConfirm(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION with a code of Finite State Machine Error
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from OPEN_CONFIRM to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        // ESTABLISHED ------------------------------------------------------------------------------------------------
        // TODO: If the local system receives a valid OPEN message (BGPOpen (Event
        //      19)), the collision detect function is processed per Section 6.8.
        //      If this connection is to be dropped due to connection collision,
        //      the local system:
        //        - sends a NOTIFICATION with a Cease,
        //        - sets the ConnectRetryTimer to zero,
        //        - releases all BGP resources,
        //        - drops the TCP connection (send TCP FIN),
        //        - increments the ConnectRetryCounter by 1,
        //        - (optionally) performs peer oscillation damping if the
        //          DampPeerOscillations attribute is set to TRUE, and
        //        - changes its state to Idle.

        protected void onManualStopEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION message with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // releases BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // sets the ConnectRetryCounter to zero
            connectRetryCounter = 0;

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onAutomaticStopEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends the NOTIFICATION message with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // releases BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // sets the ConnectRetryCounter to zero
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            callbacks.performPeerOscillationDamping();

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onHoldTimer_ExpiresEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the Error Code Hold Timer Expired
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_HOLD_TIMER_EXPIRED);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onKeepaliveTimer_ExpiresEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends a KEEPALIVE message
            callbacks.sendKeepaliveMessage();

            // restarts its KeepaliveTimer, unless the negotiated HoldTime value is zero
            if(holdTime != 0) {
                keepaliveTimer.cancel();
                keepaliveTime = 30;
                keepaliveTimer.schedule(keepaliveTimerTask, 0, 1000);
            }
        }

        protected void onOpenCollisionDumpEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION with a Cease
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_CAESE);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onTcpConnectionFails_NotifMsgVerErr_NotifMsgEstablished(String from, String to, FSMEvent event, Integer context) {
            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // releases all the BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onKeepAliveMsgEstablished(String from, String to, FSMEvent event, Integer context) {
            // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
            holdTimer.cancel();
            holdTime = 90;
            holdTimer.schedule(holdTimerTask, 0, 1000);

            // remains in the Established state
            System.out.println("Remaining in ESTABLISHED state");
        }

        protected void onUpdateMsgEstablished(String from, String to, FSMEvent event, Integer context) {
            // processes the message
            callbacks.processUpdateMessage();

            // restarts its HoldTimer, if the negotiated HoldTime value is non-zero
            if(holdTime != 0) {
                holdTimer.cancel();
                holdTime = 90;
                holdTimer.schedule(holdTimerTask, 0, 1000);
            }

            // remains in the Established state
            System.out.println("Remaining in ESTABLISHED state");
        }

        protected void onUpdateMsgErrEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with an Update error
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_UPDATE_MESSAGE_ERROR);

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }

        protected void onAnyOtherEventEstablished(String from, String to, FSMEvent event, Integer context) {
            // sends a NOTIFICATION message with the Error Code Finite State Machine Error
            callbacks.sendNotificationMessage(BGP4MessageNotification.ERR_CODE_FINITE_STATE_MACHINE_ERROR);

            // deletes all routes associated with this connection
            callbacks.deleteAllRoutes();

            // sets the ConnectRetryTimer to zero
            connectRetryTimer.cancel();
            connectRetryTime = 0;

            // releases all BGP resources
            callbacks.releaseBGPResources();

            // drops the TCP connection+
            callbacks.dropTCPConnection();

            // increments the ConnectRetryCounter by 1
            connectRetryCounter++;

            // (optionally) performs peer oscillation damping if the DampPeerOscillations attribute is set to TRUE
            if(dampPeerOscillations) {
                callbacks.performPeerOscillationDamping();
            }

            // changes its state to Idle
            System.out.println("Changing state from ESTABLISHED to IDLE");
        }
        // ------------------------------------------------------------------------------------------------------------

        protected void onConnect(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Now the state is CONNECTED");
        }
    }
}
