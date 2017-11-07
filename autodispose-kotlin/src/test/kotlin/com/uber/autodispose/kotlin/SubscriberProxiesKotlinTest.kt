package com.uber.autodispose.kotlin

import com.google.common.truth.Truth.assertThat
import io.reactivex.*
import io.reactivex.subjects.MaybeSubject
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class SubscriberProxiesKotlinTest {
    private val scopeMaybe = MaybeSubject.create<Any>()
    private val ref = AtomicReference<String>()

    @Test fun subscribeBy_ObservableSubscribeProxy() {
        Observable.just("Hello")
                .autoDisposeWith(scopeMaybe)
                .subscribeBy { ref.set(it) }
        assertThat(ref.get()).isEqualTo("Hello")
    }

    @Test fun subscribeBy_FlowableSubscribeProxy() {
        Flowable.just("Hello 2")
                .autoDisposeWith(scopeMaybe)
                .subscribeBy { ref.set(it) }
        assertThat(ref.get()).isEqualTo("Hello 2")
    }

    @Test fun subscribeBy_SingleSubscribeProxy() {
        Single.just("Hello 3")
                .autoDisposeWith(scopeMaybe)
                .subscribeBy { ref.set(it) }
        assertThat(ref.get()).isEqualTo("Hello 3")
    }

    @Test fun subscribeBy_MaybeSubscribeProxy() {
        Maybe.just("Hello 4")
                .autoDisposeWith(scopeMaybe)
                .subscribeBy { ref.set(it) }
        assertThat(ref.get()).isEqualTo("Hello 4")
    }

    @Test fun subscribeBy_CompletableSubscribeProxy() {
        Completable.complete()
                .autoDisposeWith(scopeMaybe)
                .subscribeBy { ref.set("Hello 5") }
        assertThat(ref.get()).isEqualTo("Hello 5")
    }
}
