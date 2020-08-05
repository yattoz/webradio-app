package fr.forum_thalie.tsumugi.alarm

import android.content.Context
import android.content.Intent
import fr.forum_thalie.tsumugi.Actions
import fr.forum_thalie.tsumugi.RadioService


fun playOnFallback(c: Context) {
    val a = Actions.PLAY_OR_FALLBACK
    val i = Intent(c, RadioService::class.java)
    i.putExtra("action", a.name)
    c.startService(i)
}

fun resume(c: Context)
{

}

fun resumeStop(c: Context) {
    val a = Actions.PLAY
    val i = Intent(c, RadioService::class.java)
    i.putExtra("action", a.name)
    c.startService(i)
}

fun resumePlay(c: Context) {
    val a = Actions.PLAY
    val i = Intent(c, RadioService::class.java)
    i.putExtra("action", a.name)
    c.startService(i)
}