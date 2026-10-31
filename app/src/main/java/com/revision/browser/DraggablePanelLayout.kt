package com.revision.browser

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Liquid-glass panel that can be repositioned like a home-screen icon:
 * long-press anywhere on the panel (~450 ms, without moving much), then
 * drag. Releasing saves the position in SharedPreferences and it is
 * restored on the next launch.
 *
 * The long-press is detected in dispatchTouchEvent so it works even when
 * the touch starts on a child such as the address EditText. Normal taps
 * and typing are forwarded unchanged when no drag has started.
 *
 * لوحة زجاجية قابلة للسحب بالضغط المطوّل، مثل أيقونات الشاشة الرئيسية.
 * الموضع يُحفظ ويُستعاد عند إعادة فتح التطبيق.
 */
class DraggablePanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** SharedPreferences key prefix for this panel, e.g. "drag_topBar". */
    var prefsKey: String = "drag_panel"
        set(value) {
            field = value
            restorePosition()
        }

    var onDragStarted: (() -> Unit)? = null
    var onDragEnded: (() -> Unit)? = null

    private val prefs: SharedPreferences =
        context.getSharedPreferences("revision_layout", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    private var downX = 0f
    private var downY = 0f
    private var startTranslationX = 0f
    private var startTranslationY = 0f
    private var dragging = false
    private var longPressPending = false

    private val longPressRunnable = Runnable {
        if (!longPressPending) return@Runnable
        dragging = true
        longPressPending = false
        // Cancel the child's touch (e.g. EditText selection) so the drag owns the gesture.
        val cancel = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        super.dispatchTouchEvent(cancel)
        cancel.recycle()
        alpha = 0.85f
        elevation = 24f
        Toast.makeText(context, R.string.drag_started, Toast.LENGTH_SHORT).show()
        onDragStarted?.invoke()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        restorePosition()
    }

    /** Restore the saved translation, clamped to zero if nothing was saved. */
    fun restorePosition() {
        if (!isAttachedToWindow && translationX == 0f && translationY == 0f) {
            // Still safe to apply: translation is a plain property.
        }
        translationX = prefs.getFloat("${prefsKey}_x", 0f)
        translationY = prefs.getFloat("${prefsKey}_y", 0f)
    }

    fun resetPosition() {
        translationX = 0f
        translationY = 0f
        prefs.edit()
            .putFloat("${prefsKey}_x", 0f)
            .putFloat("${prefsKey}_y", 0f)
            .apply()
    }

    private fun savePosition() {
        prefs.edit()
            .putFloat("${prefsKey}_x", translationX)
            .putFloat("${prefsKey}_y", translationY)
            .apply()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                downY = event.rawY
                startTranslationX = translationX
                startTranslationY = translationY
                longPressPending = true
                handler.postDelayed(longPressRunnable, 450L)
            }
            MotionEvent.ACTION_MOVE -> {
                if (longPressPending) {
                    val moved = abs(event.rawX - downX) + abs(event.rawY - downY)
                    if (moved > 24f) {
                        // It became a scroll/tap gesture, not a long-press.
                        longPressPending = false
                        handler.removeCallbacks(longPressRunnable)
                    }
                }
                if (dragging) {
                    val parentView = parent as? View ?: return true
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    val maxX = max(0f, (parentView.width - width).toFloat())
                    val maxY = max(0f, (parentView.height - height).toFloat())
                    // Allow moving up/left from the gravity position, clamp the rest.
                    translationX = min(maxX, max(-width.toFloat() / 2, startTranslationX + dx))
                    translationY = min(maxY, max(-height.toFloat() / 2, startTranslationY + dy))
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressPending = false
                handler.removeCallbacks(longPressRunnable)
                if (dragging) {
                    dragging = false
                    alpha = 1f
                    elevation = 8f
                    savePosition()
                    onDragEnded?.invoke()
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
