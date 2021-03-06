package blueeyes.health.metrics

import blueeyes.json.JsonAST._

private[metrics] trait TimedErrorStatReport extends AsyncStatistic[Long, Map[Long, Double]]{
  def toJValue = details map {details =>JObject(JField(config.toString, JArray(details.toList.sortWith((e1, e2) => (e1._1 > e2._1)).map(kv => JInt(kv._2.toLong)))) :: Nil) }

  protected def config: IntervalConfig
}

object TimedErrorStat {
  def apply(intervalConfig: IntervalConfig)(implicit clock: () => Long): AsyncStatistic[Long, Map[Long, Double]] = intervalConfig match{
    case e: interval => new TimedNumbersSample(e) with TimedErrorStatReport
    case eternity    => new EternityTimedNumbersSample with TimedErrorStatReport
  }
}