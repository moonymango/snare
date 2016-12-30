package stachelsau.snare.opengl;

import java.nio.Buffer;

import stachelsau.snare.util.Logger;
import stachelsau.snare.util.Logger.LogSource;

import android.opengl.GLES20;

public class GLES20Trace extends GLES20 {
    
    public static enum Mode {
        WRITE_THROUGH,
        FLUSH_MANUALLY,
        FLUSH_ON_ERROR,
        FLUSH_ON_ERROR_EXCEPTION;    
    }
    
    public static Mode sMode = Mode.FLUSH_ON_ERROR_EXCEPTION;
    public static boolean sEnable = LogSource.OPENGL_TRACE.mEnabled;
    
    public static final int RINGBUFFER_CAPACITY = 30;
    private static final String[] sRingBuffer = new String[RINGBUFFER_CAPACITY];
    private static int sPos = 0;
    
    private static void write(String msg) {
        
        int stat = GLES20.glGetError();
        if (stat != GL_NO_ERROR) {
            sRingBuffer[sPos] = msg + " --> error " + stat;    
        } else {
            sRingBuffer[sPos] = msg;
        }
        sPos = (sPos + 1) % RINGBUFFER_CAPACITY;
        
        switch (sMode) {
        case WRITE_THROUGH:
            flush();
            break;
            
        case FLUSH_ON_ERROR:
        case FLUSH_ON_ERROR_EXCEPTION:
            if (stat != GLES20.GL_NO_ERROR){
                flush();
            }
        default:
        }
        
        
    }
    
    public static void flush() {
        int pos = (sPos + 1) % RINGBUFFER_CAPACITY;
        
        while (pos != sPos) {
            if (sRingBuffer[pos] != null) {
                Logger.d(LogSource.OPENGL_TRACE, sRingBuffer[pos]);
                sRingBuffer[pos] = null;
            }
            pos = (pos + 1) % RINGBUFFER_CAPACITY;
        }
        sPos = 0;
        if (sMode == Mode.FLUSH_ON_ERROR_EXCEPTION) {
            throw new IllegalStateException("GL error.");
        }
    }
    
    public static void getError(String tag) {
        if (sEnable) write(tag);
    }
    
    public static void glClear(int mask) {
        GLES20.glClear(mask);
        if (sEnable) write("glClear: mask " + mask);
    }
    
    public static void glGenBuffers(int n, int[] buffers, int offset) {
        GLES20.glGenBuffers(n, buffers, offset);
        if (sEnable) write("glGenBuffers:");
    }
    
    public static void glBindBuffer(int target, int buffer) {
        GLES20.glBindBuffer(target, buffer);
        if (sEnable) write("glBindBuffer: target=" + target + " buffer=" + buffer);
    }
    
    public static void glBufferData(int target, int size, Buffer data, int usage) {
        GLES20.glBufferData(target, size, data, usage);
        if (sEnable) write("glBufferData: target=" + target);
    }
    
    public static void glDeleteBuffers(int n, int[] buffers, int offset) {
        GLES20.glDeleteBuffers(n, buffers, offset);
        if (sEnable) write("glDeleteBuffers:");
    }
    
    public static void glViewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
        if (sEnable) write("glViewPort: w=" + width + " h=" + height);
    }
    
    public static void glClearColor(float red, float green, float blue, float alpha) {
        GLES20.glClearColor(red, green, blue, alpha);
        if (sEnable) write("glClearColor:");
    }
    
    public static int glCreateShader(int type) {
        int s = GLES20.glCreateShader(type);
        if (sEnable) write("glCreateShader: new shader=" + s);
        return s;
    }
    
    public static void glShaderSource(int shader, String string) {
        GLES20.glShaderSource(shader, string);
        if (sEnable) write("glShaderSource: shader=" + shader + "\n" + string);
    }
    
    public static void glCompileShader(int shader) {
        GLES20.glCompileShader(shader);
        int[] result = {0};
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, result, 0);
        if (sEnable) write("glCompileShader: shader=" + shader + "status=" + result[0]);
    }
    
    public static int glCreateProgram() {
        int p = GLES20.glCreateProgram();
        if (sEnable) write("glCreateProgram: new program=" + p);
        return p;
    }
    
    public static void glAttachShader(int program, int shader) {
        GLES20.glAttachShader(program, shader);
        if (sEnable) write("glAttachShader: program=" + program + " shader=" + shader);
    }
    
    public static void glLinkProgram(int program) {
        GLES20.glLinkProgram(program);
        int[] result = {0};
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, result, 0);
        if (sEnable) write("glLinkProgram: program=" + program + " status=" + result[0]);
    }
    
    public static void glDeleteProgram(int program) {
        GLES20.glDeleteProgram(program);
        if (sEnable) write("glDeleteProgram: program=" + program);
    }
    
    public static void glDeleteShader(int shader) {
        GLES20.glDeleteShader(shader);
        if (sEnable) write("glDeleteShader: shader=" + shader);
    }
    
    public static void glUseProgram(int program) {
        GLES20.glUseProgram(program);
        if (sEnable) write("glUseProgram: program " + program);
    }
    
    public static void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int offset) {
        GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, offset);
        if (sEnable) write("glVertexAttribPointer: index=" + indx);
    }

    public static void glEnableVertexAttribArray(int index) {
        GLES20.glEnableVertexAttribArray(index);
        if (sEnable) write("glEnableVertexAttribArray: index=" + index);
    }
    
    public static void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
        GLES20.glUniformMatrix4fv(location, count, transpose, value, offset);
        if (sEnable) write("glUniformMatrix4fv: loc=" + location);
    }
    
    public static void glDrawElements(int mode, int count, int type, int offset) {
        GLES20.glDrawElements(mode, count, type, offset);
        if (sEnable) write("glDrawElements: elements=" + count);
    }
    
    public static void glDisableVertexArray(int index) {
        GLES20.glDisableVertexAttribArray(index);
        if (sEnable) write("glDisableVertexArray: index=" + index);
    }
    
    public static int glGetAttribLocation(int program, String name) {
        int loc = GLES20.glGetAttribLocation(program, name);
        if (sEnable) write("glGetAttribLocation: program=" + program + " name=" + name);
        return loc;
    }
    
    public static int glGetUniformLocation(int program, String name) {
        int loc = GLES20.glGetUniformLocation(program, name);
        if (sEnable) write("glGetUniformLocation: program=" + program + " name=" + name);
        return loc;
    }
    
    public static void glGenTextures(int n, int[] textures, int offset){
        GLES20.glGenTextures(n, textures, offset);
        if (sEnable) write("glGenTextures:");
    }
    
    public static void glBindTexture(int target, int texture) {
        GLES20.glBindTexture(target, texture);
        if (sEnable) write("glBindTextures: texture=" + texture);
    }
    
    public static void glActiveTexture(int texture) {
        GLES20.glActiveTexture(texture);
        if (sEnable) write("glActiveTexture: texture=" + texture);
    }
    
    public static void glUniform1i(int location, int x) {
        GLES20.glUniform1i(location, x);
        if (sEnable) write("glUniform1i: location=" + location);
    }
}
