/*
 * Copyright 2014-2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */

package com.mbientlab.metawear;

import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Switch;
import com.mbientlab.metawear.builder.RouteBuilder;

import org.junit.Before;
import org.junit.Test;

import bolts.Continuation;
import bolts.Task;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by etsai on 9/4/16.
 */
public class TestSwitch extends UnitTestBase {
    @Before
    public void setup() throws Exception {
        junitPlatform.boardInfo= new MetaWearBoardInfo(Switch.class);
        connectToBoard();
    }

    @Test
    public void subscribe() {
        byte[] expected= new byte[] {0x1, 0x1, 0x1};

        mwBoard.getModule(Switch.class).state().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(null);
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                return null;
            }
        });

        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void unsubscribe() {
        byte[] expected= new byte[] {0x1, 0x1, 0x0};

        mwBoard.getModule(Switch.class).state().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(null);
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                task.getResult().unsubscribe(0);
                return null;
            }
        });

        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void handlePressed() {
        final long expected= 1;
        final long[] actual= new long[1];

        mwBoard.getModule(Switch.class).state().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object ... env) {
                        ((long[]) env[0])[0]= data.value(Long.class);
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                task.getResult().setEnvironment(0, actual);
                return null;
            }
        });

        sendMockResponse(new byte[] {0x1, 0x1, 0x1});
        assertEquals(expected, actual[0]);
    }

    @Test
    public void handleReleased() {
        final long expected= 0;
        final long[] actual= new long[1];

        mwBoard.getModule(Switch.class).state().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object ... env) {
                        ((long[]) env[0])[0]= data.value(Long.class);
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                task.getResult().setEnvironment(0, actual);
                return null;
            }
        });

        sendMockResponse(new byte[] {0x1, 0x1, 0x0});
        assertEquals(expected, actual[0]);
    }

    @Test
    public void readCurrentState() {
        byte[] expected = new byte[] {0x01, (byte) 0x81};

        mwBoard.getModule(Switch.class).readCurrentStateAsync();

        assertArrayEquals(expected, junitPlatform.getLastCommand());
    }

    @Test
    public void readCurrentStateData() {
        byte[] expected = new byte[] {0x01, 0x00};
        final byte[] actual = new byte[2];

        mwBoard.getModule(Switch.class).readCurrentStateAsync().continueWith(new Continuation<Byte, Void>() {
            @Override
            public Void then(Task<Byte> task) throws Exception {
                actual[0] = task.getResult();
                return null;
            }
        });
        mwBoard.getModule(Switch.class).readCurrentStateAsync().continueWith(new Continuation<Byte, Void>() {
            @Override
            public Void then(Task<Byte> task) throws Exception {
                actual[1] = task.getResult();
                return null;
            }
        });

        sendMockResponse(new byte[] {0x01, (byte) 0x81, 0x1});
        sendMockResponse(new byte[] {0x01, (byte) 0x81, 0x0});
        assertArrayEquals(expected, actual);
    }
}
