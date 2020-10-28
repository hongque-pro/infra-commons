package com.labijie.infra.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.*
import com.esotericsoftware.kryo.util.Pool
import java.nio.ByteBuffer
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-02-16
 */
abstract class PooledKryo(poolSize: Int, useTimesPerBorrow: Int, private val outputInitSizeBytes:Int = 4 * 1024) {

    constructor(poolSize: Int = Runtime.getRuntime().availableProcessors() * 2) : this(poolSize, 1)

    private val kryoPool = newPool(poolSize)
    private val outputBufferPool = newByteBufferOutputPool(poolSize * Math.max(1, useTimesPerBorrow))
    private val outputPool = newOutputPool(poolSize * Math.max(1, useTimesPerBorrow))



    private fun newPool(poolSize: Int): Pool<Kryo> {
        return object : Pool<Kryo>(true, false, poolSize) {
            override fun create(): Kryo {
                return createKryo()
            }
        }
    }

    private fun newInputPool(poolSize: Int): Pool<Input> {
        return object : Pool<Input>(true, true, poolSize) {
            override fun create(): Input {
                return Input()
            }
        }
    }

    private fun newOutputPool(poolSize: Int): Pool<Output> {
        return object : Pool<Output>(true, true, poolSize) {
            override fun create(): Output {
                return Output(outputInitSizeBytes, -1)
            }
        }
    }

    private fun newByteBufferOutputPool(poolSize: Int): Pool<ByteBufferOutput> {
        return object : Pool<ByteBufferOutput>(true, true, poolSize) {
            override fun create(): ByteBufferOutput {
                return ByteBufferOutput(outputInitSizeBytes, -1)
            }
        }
    }

    private fun newByteBufferInputPool(poolSize: Int): Pool<ByteBufferInput> {
        return object : Pool<ByteBufferInput>(true, true, poolSize) {
            override fun create(): ByteBufferInput {
                return ByteBufferInput()
            }
        }
    }

    protected abstract fun createKryo(): Kryo

    fun serializeBuffer(data: Any): Pooled<ByteBuffer> {
        val output = outputBufferPool.obtain()
        try {
            output.byteBuffer.clear()
            val kryo = kryoPool.obtain()
            try {
                kryo.writeObject(output, data)
            } finally {
                kryoPool.free(kryo)
            }
            output.flush()

            output.byteBuffer.run {
                this.limit(output.total().toInt())
                this.position(0)
            }
        } catch (ex:Throwable) {
            outputBufferPool.free(output)
            throw ex
        }
        return Pooled(output.byteBuffer){ outputBufferPool.free(output) }
    }

    fun <T : Any> deserializeByteBuffer(data: ByteBuffer, clazz: KClass<T>): T {
        val input = ByteBufferInput(data)
        val kryo = kryoPool.obtain()
        try {
            return kryo.readObject(input, clazz.java)
        } finally {
            kryoPool.free(kryo)
            input.close()
        }
    }

    fun serialize(data: Any): ByteArray {
        val output = outputPool.obtain()
        try {
            output.reset()
            val kryo = kryoPool.obtain()
            try {
                kryo.writeObject(output, data)
            } finally {
                kryoPool.free(kryo)
            }
            output.flush()
            return output.toBytes()
        } catch (ex:Throwable) {
            outputPool.free(output)
            throw ex
        }finally {
            outputPool.free(output)
        }
    }

    fun <T : Any> deserialize(data: ByteArray, clazz: KClass<T>): T {
        val input = Input(data)
        val kryo = kryoPool.obtain()
        try {
            return kryo.readObject(input, clazz.java)
        } finally {
            kryoPool.free(kryo)
            input.close()
        }
    }
}