# Module logger-coroutines

Optional coroutines support for KMP Logger: safe `LogContext` propagation across suspension points
and thread hops. Depends on `:logger` via `api`, so consumers name its types transitively.

The context travels in the `CoroutineContext`, so no coroutine can observe another's context. On
JVM/Android only, the element is also a `ThreadContextElement`, mirroring the context onto each
thread the coroutine resumes on, so even plain unbound loggers see it there. On every other target
the context stays in the coroutine context and never touches thread-local state.

# Package dev.shivathapaa.logger.coroutines

`withLogContext(ctx) { }` carries a `LogContext` in the coroutine context via `LogContextElement`.
Read it with `currentLogContext()`, or bind it to a logger with `Logger.withActiveLogContext()`.
