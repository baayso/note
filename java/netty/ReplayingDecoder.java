/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Signal;
import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * {@link ByteToMessageDecoder}的一种特殊变体，它可以在阻塞I/O范式中实现非阻塞解码器。
 * <p>
 * {@link ReplayingDecoder}和{@link ByteToMessageDecoder}之间的最大区别在于，
 * {@link ReplayingDecoder}可以让你就像已经接收到所有必需字节一样实现{@code decode()}和{@code decodeLast()}方法，
 * 而无需检查所需字节是否可用。例如，以下{@link ByteToMessageDecoder}实现：
 * <pre>
 * public class IntegerHeaderFrameDecoder extends {@link ByteToMessageDecoder} {
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     if (buf.readableBytes() &lt; 4) {
 *        return;
 *     }
 *
 *     buf.markReaderIndex();
 *     int length = buf.readInt();
 *
 *     if (buf.readableBytes() &lt; length) {
 *        buf.resetReaderIndex();
 *        return;
 *     }
 *
 *     out.add(buf.readBytes(length));
 *   }
 * }
 * </pre>
 * 使用{@link ReplayingDecoder}可以进行如下简化:
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf) throws Exception {
 *
 *     out.add(buf.readBytes(buf.readInt()));
 *   }
 * }
 * </pre>
 *
 * <h3>这是怎么实现的呢？</h3>
 * <p>
 * {@link ReplayingDecoder}传递了一个特殊的{@link ByteBuf}实现，
 * 当缓冲区中没有足够的数据时，该实现会抛出某种类型的{@link Error}。
 * 在上面的{@code IntegerHeaderFrameDecoder}中，
 * 你只是假设在调用{@code buf.readInt()}时缓冲区中会有4个或更多字节。
 * 如果缓冲区中确实有4个字节，它将像你期望的那样返回整数header(报头)。
 * 否则，将抛出{@link Error}并将控制返回给{@link ReplayingDecoder}。
 * 如果{@link ReplayingDecoder}捕捉到{@link Error}，
 * 那么它会将缓冲区的{@code readerIndex}重置为“初始”位置(即缓冲区的开始)，
 * 并在缓冲区接收到更多数据时再次调用{@code decode(..)}方法。
 * <p>
 * 请注意，{@link ReplayingDecoder}总是抛出相同的被缓存的{@link Error}实例，
 * 以避免创建新{@link Error}，并在每次抛出时填充其堆栈跟踪的开销。
 *
 * <h3>局限性</h3>
 * <p>
 * 为了简单起见，{@link ReplayingDecoder}强制你遵守一些限制：
 * <ul>
 * <li>禁止某些缓冲区操作。</li>
 * <li>与上面的示例不同，如果网络速度较慢且消息格式复杂，则性能可能会更差。
 *     在这种情况下，您的解码器可能不得不一次又一次地解码相同的消息部分。</li>
 * <li>你必须记住{@code decode(..)}方法可能被多次调用来解码单个消息。
 *     例如，以下代码将不能正常使用:
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public void decode(.., {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     // 一条消息包含两个整数。
 *     values.offer(buf.readInt());
 *     values.offer(buf.readInt());
 *
 *     // 这个断言将间歇性地失败，因为上面的values.offer()可能会被调用两次以上！
 *     assert values.size() == 2;
 *     out.add(values.poll() + values.poll());
 *   }
 * }</pre>
 *      正确的实现如下所示，你还可以利用“检查点”特性，
 *      下一部分将对其进行详细说明。
 * <pre> public class MyDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *   private final Queue&lt;Integer&gt; values = new LinkedList&lt;Integer&gt;();
 *
 *   {@code @Override}
 *   public void decode(.., {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *
 *     // 还原自上次部分解码以来可能已更改的变量的状态。
 *     values.clear();
 *
 *     // 一条消息包含两个整数。
 *     values.offer(buf.readInt());
 *     values.offer(buf.readInt());
 *
 *     // 现在这个断言永远不会失败。
 *     assert values.size() == 2;
 *     out.add(values.poll() + values.poll());
 *   }
 * }</pre>
 *     </li>
 * </ul>
 *
 * <h3>提高性能</h3>
 * <p>
 * 幸运的是，使用{@code checkpoint()}方法可以显著提高复杂解码器实现的性能。
 * {@code checkpoint()}方法更新缓冲区的“初始”位置，
 * 以便{@link ReplayingDecoder}将缓冲区的{@code readerIndex}
 * 重置到调用{@code checkpoint()}方法时的最后位置。
 *
 * <h4>使用{@link Enum}调用{@code checkpoint(T)}</h4>
 * <p>
 * Although you can just use {@code checkpoint()} method and manage the state
 * of the decoder by yourself, the easiest way to manage the state of the
 * decoder is to create an {@link Enum} type which represents the current state
 * of the decoder and to call {@code checkpoint(T)} method whenever the state
 * changes.  You can have as many states as you want depending on the
 * complexity of the message you want to decode:
 * 虽然你可以只使用{@code checkpoint()}方法并自己管理解码器的状态，
 * 但是管理解码器状态的最简单方法是创建一个表示解码器当前状态的{@link Enum}类型，
 * 并在状态改变时调用{@code checkpoint(T)}方法。
 * 根据要解码的消息的复杂性，可以根据需要设置任意多个状态：
 *
 * <pre>
 * public enum MyDecoderState {
 *   READ_LENGTH,
 *   READ_CONTENT;
 * }
 *
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>MyDecoderState</strong>&gt; {
 *
 *   private int length;
 *
 *   public IntegerHeaderFrameDecoder() {
 *     // Set the initial state.
 *     <strong>super(MyDecoderState.READ_LENGTH);</strong>
 *   }
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *     switch (state()) {
 *     case READ_LENGTH:
 *       length = buf.readInt();
 *       <strong>checkpoint(MyDecoderState.READ_CONTENT);</strong>
 *     case READ_CONTENT:
 *       ByteBuf frame = buf.readBytes(length);
 *       <strong>checkpoint(MyDecoderState.READ_LENGTH);</strong>
 *       out.add(frame);
 *       break;
 *     default:
 *       throw new Error("Shouldn't reach here.");
 *     }
 *   }
 * }
 * </pre>
 *
 * <h4>调用没有参数的 {@code checkpoint()}</h4>
 * <p>
 * 管理解码器状态的另一种方式是你自己去管理。
 * <pre>
 * public class IntegerHeaderFrameDecoder
 *      extends {@link ReplayingDecoder}&lt;<strong>{@link Void}</strong>&gt; {
 *
 *   <strong>private boolean readLength;</strong>
 *   private int length;
 *
 *   {@code @Override}
 *   protected void decode({@link ChannelHandlerContext} ctx,
 *                           {@link ByteBuf} buf, List&lt;Object&gt; out) throws Exception {
 *     if (!readLength) {
 *       length = buf.readInt();
 *       <strong>readLength = true;</strong>
 *       <strong>checkpoint();</strong>
 *     }
 *
 *     if (readLength) {
 *       ByteBuf frame = buf.readBytes(length);
 *       <strong>readLength = false;</strong>
 *       <strong>checkpoint();</strong>
 *       out.add(frame);
 *     }
 *   }
 * }
 * </pre>
 *
 * <h3>用pipeline(管道)中的一个解码器替换另一个解码器</h3>
 * <p>
 * If you are going to write a protocol multiplexer, you will probably want to
 * replace a {@link ReplayingDecoder} (protocol detector) with another
 * {@link ReplayingDecoder}, {@link ByteToMessageDecoder} or {@link MessageToMessageDecoder}
 * (actual protocol decoder).
 * It is not possible to achieve this simply by calling
 * {@link ChannelPipeline#replace(ChannelHandler, String, ChannelHandler)}, but
 * some additional steps are required:
 * 如果要编写一个协议多路复用器，
 * 则可能需要用另一个{@link ReplayingDecoder}，
 * {@link ByteToMessageDecoder}或{@link MessageToMessageDecoder}(实际协议解码器)
 * 替换{@link ReplayingDecoder}(协议检测器)。 
 * 不能只通过调用{@link ChannelPipeline#replace(ChannelHandler, String, ChannelHandler)}来实现此目的，
 * 还需要一些额外的步骤：
 * <pre>
 * public class FirstDecoder extends {@link ReplayingDecoder}&lt;{@link Void}&gt; {
 *
 *     {@code @Override}
 *     protected void decode({@link ChannelHandlerContext} ctx,
 *                             {@link ByteBuf} buf, List&lt;Object&gt; out) {
 *         ...
 *         // Decode the first message
 *         Object firstMessage = ...;
 *
 *         // Add the second decoder
 *         ctx.pipeline().addLast("second", new SecondDecoder());
 *
 *         if (buf.isReadable()) {
 *             // Hand off the remaining data to the second decoder
 *             out.add(firstMessage);
 *             out.add(buf.readBytes(<b>super.actualReadableBytes()</b>));
 *         } else {
 *             // Nothing to hand off
 *             out.add(firstMessage);
 *         }
 *         // Remove the first decoder (me)
 *         ctx.pipeline().remove(this);
 *     }
 * </pre>
 * @param <S>
 *        状态类型，通常是{@link Enum}；如果不使用状态管理，则使用{@link Void}
 */
public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder {

    static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class, "REPLAY");

    private final ReplayingDecoderByteBuf replayable = new ReplayingDecoderByteBuf();
    private S state;
    private int checkpoint = -1;

    /**
     * Creates a new instance with no initial state (i.e: {@code null}).
     */
    protected ReplayingDecoder() {
        this(null);
    }

    /**
     * Creates a new instance with the specified initial state.
     */
    protected ReplayingDecoder(S initialState) {
        state = initialState;
    }

    /**
     * Stores the internal cumulative buffer's reader position.
     */
    protected void checkpoint() {
        checkpoint = internalBuffer().readerIndex();
    }

    /**
     * Stores the internal cumulative buffer's reader position and updates
     * the current decoder state.
     */
    protected void checkpoint(S state) {
        checkpoint();
        state(state);
    }

    /**
     * Returns the current state of this decoder.
     * @return the current state of this decoder
     */
    protected S state() {
        return state;
    }

    /**
     * Sets the current state of this decoder.
     * @return the old state of this decoder
     */
    protected S state(S newState) {
        S oldState = state;
        state = newState;
        return oldState;
    }

    @Override
    final void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
        try {
            replayable.terminate();
            if (cumulation != null) {
                callDecode(ctx, internalBuffer(), out);
                decodeLast(ctx, replayable, out);
            } else {
                replayable.setCumulation(Unpooled.EMPTY_BUFFER);
                decodeLast(ctx, replayable, out);
            }
        } catch (Signal replay) {
            // Ignore
            replay.expect(REPLAY);
        }
    }

    @Override
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        replayable.setCumulation(in);
        try {
            while (in.isReadable()) {
                int oldReaderIndex = checkpoint = in.readerIndex();
                int outSize = out.size();

                if (outSize > 0) {
                    fireChannelRead(ctx, out, outSize);
                    out.clear();

                    // Check if this handler was removed before continuing with decoding.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See:
                    // - https://github.com/netty/netty/issues/4635
                    if (ctx.isRemoved()) {
                        break;
                    }
                    outSize = 0;
                }

                S oldState = state;
                int oldInputLength = in.readableBytes();
                try {
                    decodeRemovalReentryProtection(ctx, replayable, out);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    if (outSize == out.size()) {
                        if (oldInputLength == in.readableBytes() && oldState == state) {
                            throw new DecoderException(
                                    StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound " +
                                    "data or change its state if it did not decode anything.");
                        } else {
                            // Previous data has been discarded or caused state transition.
                            // Probably it is reading on.
                            continue;
                        }
                    }
                } catch (Signal replay) {
                    replay.expect(REPLAY);

                    // Check if this handler was removed before continuing the loop.
                    // If it was removed, it is not safe to continue to operate on the buffer.
                    //
                    // See https://github.com/netty/netty/issues/1664
                    if (ctx.isRemoved()) {
                        break;
                    }

                    // Return to the checkpoint (or oldPosition) and retry.
                    int checkpoint = this.checkpoint;
                    if (checkpoint >= 0) {
                        in.readerIndex(checkpoint);
                    } else {
                        // Called by cleanup() - no need to maintain the readerIndex
                        // anymore because the buffer has been released already.
                    }
                    break;
                }

                if (oldReaderIndex == in.readerIndex() && oldState == state) {
                    throw new DecoderException(
                           StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data " +
                           "or change its state if it decoded something.");
                }
                if (isSingleDecode()) {
                    break;
                }
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Throwable cause) {
            throw new DecoderException(cause);
        }
    }
}
