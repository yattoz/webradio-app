package io.r_a_d.radio2.ui.songs.request

import kotlin.math.exp
import kotlin.math.max

/*
//PHP cooldown calculator

function pretty_cooldown($lp, $lr, $rc) {
	$delay = delay($rc);
	$now = time();
	$cd = intval(max($lp + $delay - $now, $lr + $delay - $now));
	if ($cd <= 0)
		return "Request";
	$days = intdiv_1($cd, 86400);
	$cd = $cd % 86400;
	$hours = intdiv_1($cd, 3600);
	$cd = $cd % 3600;
	$minutes = intdiv_1($cd, 60);
	$seconds = $cd % 60;
	if ($days > 0)
		return "Requestable in ".$days."d".$hours."h";
	else if ($hours > 0)
		return "Requestable in ".$hours."h".$minutes."m";
	else if ($minutes > 0)
		return "Requestable in ".$minutes."m".$seconds."s";
	return "Request";
}
function requestable($lastplayed, $requests) {
	$delay = delay($requests);
	return (time() - $lastplayed) > $delay;
}
function delay($priority) {
	// priority is 30 max
		if ($priority > 30)
			$priority = 30;
		// between 0 and 7 return magic
		if ($priority >= 0 and $priority <= 7)
			$cd = -11057 * $priority * $priority + 172954 * $priority + 81720;
		// if above that, return magic crazy numbers
		else
			$cd = (int) (599955 * exp(0.0372 * $priority) + 0.5);
		return $cd / 2;
}
 */

// this function implements the magic delay used on R/a/dio website:
// https://github.com/R-a-dio/site/blob/develop/app/start/global.php#L125
// (Seriously guys, what were you thinking with these crazy magic numbers...)
fun delay(rawPriority: Int) : Int {
    val priority = if (rawPriority > 30) 30 else rawPriority
    val coolDown : Int =
        if (priority in 0..7)
            -11057 * priority * priority + 172954 * priority + 81720
        else
            (599955 * exp(0.0372 * (priority.toDouble()) + 0.5)).toInt()
    return coolDown/2
}

// I tweaked this to report in a single point whether the song is requestable or not
fun coolDown(lastPlayed: Int?, lastRequest: Int?, requestsNbr: Int?) : Long {
    if (requestsNbr == null || lastPlayed == null || lastRequest == null)
        return Long.MAX_VALUE // maximum positive value : the song won't be requestable

    val delay = delay(requestsNbr)
    val now = (System.currentTimeMillis() / 1000)
    return max(lastPlayed, lastRequest) + delay - now
    // if coolDown < 0, the song is requestable.
}
