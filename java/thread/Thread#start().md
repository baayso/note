# `new Thread().start()`

## 1. 在Java中，调用`start()`方法启动线程
```java
/**
 * 启动线程，Java 虚拟机调用该线程的 run() 方法。 
 */
public synchronized void start() {
    /**
     * This method is not invoked for the main method thread or "system"
     * group threads created/set up by the VM. Any new functionality added
     * to this method in the future may have to also be added to the VM.
     *
     * A zero status value corresponds to state "NEW".
     *
     * 检查线程状态，如果不为0（0状态值对应于状态“NEW”）则抛出异常，保证一个线程只能启动一次。
     */
    if (threadStatus != 0)
        throw new IllegalThreadStateException();

    /* Notify the group that this thread is about to be started
     * so that it can be added to the group's list of threads
     * and the group's unstarted count can be decremented. */
    group.add(this);

    boolean started = false;
    try {
        // start0() 是一个native方法
        start0();
        started = true;
    } finally {
        try {
            if (!started) {
                group.threadStartFailed(this);
            }
        } catch (Throwable ignore) {
            /* do nothing. If start0 threw a Throwable then
              it will be passed up the call stack */
        }
    }
}

private native void start0();
```

## 2. `start0()`native方法的实现
* `Thread.c`(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/native/java/lang/Thread.c#L44)
  ```c
  #include "jni.h"
  #include "jvm.h"

  #include "java_lang_Thread.h"

  #define THD "Ljava/lang/Thread;"
  #define OBJ "Ljava/lang/Object;"
  #define STE "Ljava/lang/StackTraceElement;"
  #define STR "Ljava/lang/String;"

  #define ARRAY_LENGTH(a) (sizeof(a)/sizeof(a[0]))

  static JNINativeMethod methods[] = {
      // Thread.c文件里实现是调用了JVM内的 JVM_StartThread 函数。我们在去查看一下 HotSpot 虚拟机对应的源代码。
      {"start0",           "()V",        (void *)&JVM_StartThread},
      {"stop0",            "(" OBJ ")V", (void *)&JVM_StopThread},
      {"isAlive",          "()Z",        (void *)&JVM_IsThreadAlive},
      {"suspend0",         "()V",        (void *)&JVM_SuspendThread},
      {"resume0",          "()V",        (void *)&JVM_ResumeThread},
      {"setPriority0",     "(I)V",       (void *)&JVM_SetThreadPriority},
      {"yield",            "()V",        (void *)&JVM_Yield},
      {"sleep",            "(J)V",       (void *)&JVM_Sleep},
      {"currentThread",    "()" THD,     (void *)&JVM_CurrentThread},
      {"countStackFrames", "()I",        (void *)&JVM_CountStackFrames},
      {"interrupt0",       "()V",        (void *)&JVM_Interrupt},
      {"isInterrupted",    "(Z)Z",       (void *)&JVM_IsInterrupted},
      {"holdsLock",        "(" OBJ ")Z", (void *)&JVM_HoldsLock},
      {"getThreads",        "()[" THD,   (void *)&JVM_GetAllThreads},
      {"dumpThreads",      "([" THD ")[[" STE, (void *)&JVM_DumpThreads},
      {"setNativeName",    "(" STR ")V", (void *)&JVM_SetNativeThreadName},
  };

  #undef THD
  #undef OBJ
  #undef STE
  #undef STR

  JNIEXPORT void JNICALL
  Java_java_lang_Thread_registerNatives(JNIEnv *env, jclass cls)
  {
      (*env)->RegisterNatives(env, cls, methods, ARRAY_LENGTH(methods));
  }
  ```
* JVM内的`JVM_StartThread`函数
  * jvm.h(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/jdk/src/share/javavm/export/jvm.h#L233)
  * jvm.cpp(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/prims/jvm.cpp#L3076)
    ```cpp
    JVM_ENTRY(void, JVM_StartThread(JNIEnv* env, jobject jthread))
      JVMWrapper("JVM_StartThread");
      JavaThread *native_thread = NULL;

      // We cannot hold the Threads_lock when we throw an exception,
      // due to rank ordering issues. Example:  we might need to grab the
      // Heap_lock while we construct the exception.
      bool throw_illegal_thread_state = false;

      // We must release the Threads_lock before we can post a jvmti event
      // in Thread::start.
      {
        // Ensure that the C++ Thread and OSThread structures aren't freed before
        // we operate.
        MutexLocker mu(Threads_lock);

        // Since JDK 5 the java.lang.Thread threadStatus is used to prevent
        // re-starting an already started thread, so we should usually find
        // that the JavaThread is null. However for a JNI attached thread
        // there is a small window between the Thread object being created
        // (with its JavaThread set) and the update to its threadStatus, so we
        // have to check for this
        if (java_lang_Thread::thread(JNIHandles::resolve_non_null(jthread)) != NULL) {
          throw_illegal_thread_state = true;
        } else {
          // We could also check the stillborn flag to see if this thread was already stopped, but
          // for historical reasons we let the thread detect that itself when it starts running

          jlong size =
                 java_lang_Thread::stackSize(JNIHandles::resolve_non_null(jthread));
          // Allocate the C++ Thread structure and create the native thread.  The
          // stack size retrieved from java is signed, but the constructor takes
          // size_t (an unsigned type), so avoid passing negative values which would
          // result in really large stacks.
          size_t sz = size > 0 ? (size_t) size : 0;
          
          // 这行代码是才真正创建线程，thread_entry需要执行的方法(也就是 Java Thread 对象的 run() 方法),sz为栈大小，默认传的是0
          native_thread = new JavaThread(&thread_entry, sz);

          // At this point it may be possible that no osthread was created for the
          // JavaThread due to lack of memory. Check for this situation and throw
          // an exception if necessary. Eventually we may want to change this so
          // that we only grab the lock if the thread was created successfully -
          // then we can also do this check and throw the exception in the
          // JavaThread constructor.
          if (native_thread->osthread() != NULL) {
            // Note: the current thread is not being used within "prepare".
            native_thread->prepare(jthread);
          }
        }
      }

      if (throw_illegal_thread_state) {
        THROW(vmSymbols::java_lang_IllegalThreadStateException());
      }

      assert(native_thread != NULL, "Starting null thread?");

      if (native_thread->osthread() == NULL) {
        // No one should hold a reference to the 'native_thread'.
        delete native_thread;
        if (JvmtiExport::should_post_resource_exhausted()) {
          JvmtiExport::post_resource_exhausted(
            JVMTI_RESOURCE_EXHAUSTED_OOM_ERROR | JVMTI_RESOURCE_EXHAUSTED_THREADS,
            "unable to create new native thread");
        }
        THROW_MSG(vmSymbols::java_lang_OutOfMemoryError(),
                  "unable to create new native thread");
      }

      Thread::start(native_thread);

    JVM_END
    ```
* `JavaThread`(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/share/vm/runtime/thread.cpp#L1566)
  ```cpp
  JavaThread::JavaThread(ThreadFunction entry_point, size_t stack_sz) :
    Thread()
  #if INCLUDE_ALL_GCS
    , _satb_mark_queue(&_satb_mark_queue_set),
    _dirty_card_queue(&_dirty_card_queue_set)
  #endif // INCLUDE_ALL_GCS
  {
    if (TraceThreadEvents) {
      tty->print_cr("creating thread %p", this);
    }
    initialize();
    _jni_attach_state = _not_attaching_via_jni;
    set_entry_point(entry_point);
    // Create the native thread itself.
    // %note runtime_23
    os::ThreadType thr_type = os::java_thread;
    thr_type = entry_point == &compiler_thread_entry ? os::compiler_thread :
                                                       os::java_thread;
    // 这里调用了不同平台（Linux、Windows等）的真实创建线程的函数
    os::create_thread(this, thr_type, stack_sz);
    // The _osthread may be NULL here because we ran out of memory (too many threads active).
    // We need to throw and OutOfMemoryError - however we cannot do this here because the caller
    // may hold a lock and all locks must be unlocked before throwing the exception (throwing
    // the exception consists of creating the exception object & initializing it, initialization
    // will leave the VM via a JavaCall and then all locks must be unlocked).
    //
    // The thread is still suspended when we reach here. Thread must be explicit started
    // by creator! Furthermore, the thread must also explicitly be added to the Threads list
    // by calling Threads:add. The reason why this is not done here, is because the thread
    // object must be fully initialized (take a look at JVM_Start)
  }
  ```
* Linux中`os::create_thread()`函数的实现
  * `os::create_thread()`(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/os/linux/vm/os_linux.cpp#L845)
  * 最终是调用的`pthread_create()`函数创建的线程(https://github.com/AdoptOpenJDK/openjdk-jdk8u/blob/master/hotspot/src/os/linux/vm/os_linux.cpp#L914)
  ```cpp
  bool os::create_thread(Thread* thread, ThreadType thr_type, size_t stack_size) {
    assert(thread->osthread() == NULL, "caller responsible");

    // Allocate the OSThread object
    OSThread* osthread = new OSThread(NULL, NULL);
    if (osthread == NULL) {
      return false;
    }

    // set the correct thread state
    osthread->set_thread_type(thr_type);

    // Initial state is ALLOCATED but not INITIALIZED
    osthread->set_state(ALLOCATED);

    thread->set_osthread(osthread);

    // init thread attributes
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

    // stack size
    if (os::Linux::supports_variable_stack_size()) {
      // calculate stack size if it's not specified by caller
      if (stack_size == 0) {
        stack_size = os::Linux::default_stack_size(thr_type);

        switch (thr_type) {
        case os::java_thread:
          // Java threads use ThreadStackSize which default value can be
          // changed with the flag -Xss
          assert (JavaThread::stack_size_at_create() > 0, "this should be set");
          stack_size = JavaThread::stack_size_at_create();
          break;
        case os::compiler_thread:
          if (CompilerThreadStackSize > 0) {
            stack_size = (size_t)(CompilerThreadStackSize * K);
            break;
          } // else fall through:
            // use VMThreadStackSize if CompilerThreadStackSize is not defined
        case os::vm_thread:
        case os::pgc_thread:
        case os::cgc_thread:
        case os::watcher_thread:
          if (VMThreadStackSize > 0) stack_size = (size_t)(VMThreadStackSize * K);
          break;
        }
      }

      stack_size = MAX2(stack_size, os::Linux::min_stack_allowed);
      pthread_attr_setstacksize(&attr, stack_size);
    } else {
      // let pthread_create() pick the default value.
    }

    // glibc guard page
    pthread_attr_setguardsize(&attr, os::Linux::default_guard_size(thr_type));

    ThreadState state;

    {
      // Serialize thread creation if we are running with fixed stack LinuxThreads
      bool lock = os::Linux::is_LinuxThreads() && !os::Linux::is_floating_stack();
      if (lock) {
        os::Linux::createThread_lock()->lock_without_safepoint_check();
      }

      pthread_t tid;
      // 最终是调用的 pthread_create() 函数创建的线程
      int ret = pthread_create(&tid, &attr, (void* (*)(void*)) java_start, thread);

      pthread_attr_destroy(&attr);

      if (ret != 0) {
        if (PrintMiscellaneous && (Verbose || WizardMode)) {
          perror("pthread_create()");
        }
        // Need to clean up stuff we've allocated so far
        thread->set_osthread(NULL);
        delete osthread;
        if (lock) os::Linux::createThread_lock()->unlock();
        return false;
      }

      // Store pthread info into the OSThread
      osthread->set_pthread_id(tid);

      // Wait until child thread is either initialized or aborted
      {
        Monitor* sync_with_child = osthread->startThread_lock();
        MutexLockerEx ml(sync_with_child, Mutex::_no_safepoint_check_flag);
        while ((state = osthread->get_state()) == ALLOCATED) {
          sync_with_child->wait(Mutex::_no_safepoint_check_flag);
        }
      }

      if (lock) {
        os::Linux::createThread_lock()->unlock();
      }
    }

    // Aborted due to thread limit being reached
    if (state == ZOMBIE) {
        thread->set_osthread(NULL);
        delete osthread;
        return false;
    }

    // The thread is returned suspended (in state INITIALIZED),
    // and is started higher up in the call chain
    assert(state == INITIALIZED, "race condition");
    return true;
  }
  ```
