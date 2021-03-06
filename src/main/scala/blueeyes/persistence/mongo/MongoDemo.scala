package blueeyes.persistence.mongo

import scala.collection.IterableView
import MongoQueryBuilder._
import MongoImplicits._
import blueeyes.concurrent.Future
import blueeyes.json.JsonAST._
import blueeyes.json.{JsonParser, JPath, Printer}
import net.lag.configgy.Configgy
import scalaz.Scalaz._
import java.util.concurrent.CountDownLatch

object MongoDemo extends MongoImplicits{


  private val jObject  = JObject(JField("address", JObject( JField("city", JString("A")) :: JField("street", JString("1")) :: JField("code", JInt(1)) :: Nil)) :: JField("location", JArray(List(JInt(40), JInt(40), JString("40")))) :: Nil)
  private val jObject1 = JObject(JField("address", JObject( JField("city", JString("B")) :: JField("street", JString("2")) :: JField("code", JInt(2)) :: Nil)) :: JField("location", JArray(List(JInt(40), JInt(40)))) :: Nil)
  private val jObject2 = JObject(JField("address", JObject( JField("city", JString("C")) :: JField("street", JString("3")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject3 = JObject(JField("address", JObject( JField("city", JString("E")) :: JField("street", JString("4")) :: JField("code", JInt(1)) :: Nil)) :: Nil)
  private val jObject6 = JObject(JField("address", JString("ll")) :: Nil)
  private val countJObject = JObject(JField("count", JInt(1)) :: Nil)
  private val count1 = JObject(JField("value", JString("1")) :: Nil)

  private val jobjectsWithArray = JsonParser.parse("""{ "foo" : [{"shape" : "square", "color" : "purple", "thick" : false}, {"shape" : "circle","color" : "red","thick" : true}] } """).asInstanceOf[JObject] :: Nil

  Configgy.configure("/etc/default/blueeyes.conf")

  val collection = "my-collection"

  val realMongo = new RealMongo(Configgy.config.configMap("mongo"))

  val database  = realMongo.database( "mydb" )

  val short  = """{"adId":"livingSocialV3","adCode":"xxxxxxxxxxx","properties":{"width":"300","height":"250","advertiserId":"all","backupImageUrl":"http://static.socialmedia.com/ads/LivingSocial/CupCake/LivingSocial_Baseline_DC.jpg","clickthroughUrl":"http://www.livingsocial.com","channelId":"livingSocialChannel","campaignId":"livingSocialCampaign","groupId":"group1","clickTag":""}}"""
  val long   = """{"adId":"livingSocialV3","adCode":"xxxxxxxxxxx","properties":{"width":"300","height":"250","advertiserId":"all","backupImageUrl":"http://static.socialmedia.com/ads/LivingSocial/CupCake/LivingSocial_Baseline_DC.jpg","clickthroughUrl":"http://www.livingsocial.com","channelId":"livingSocialChannel","campaignId":"livingSocialCampaign","groupId":"group1","clickTag":""}}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   """

  def main(args: Array[String]){
//    database(remove.from(collection))

//    database(dropIndexes.on(collection))
//    try{
//      database(ensureUniqueIndex("index").on(collection, "address.city", "address.street"))
//      database(verified(insert(jObject).into(collection)))
//      database(verified(insert(jObject).into(collection)))
//    }
//    finally database(remove.from(collection))

//    benchamrk()
//    demoSelectOne

    demoSelect
    
//    demoUpdate1

//    demoRemove

//    demoDistinct

//    demoGroup
//    demoGeoNear
  }

  private def demoGeoNear{
    println("------------demoGeoNear------------------")
    database(ensureUniqueIndex("location").on("location").in(collection).geospatial("location"))
    var count = new CountDownLatch(1)
    var f = database(insert(jObject, jObject3).into(collection))
    f.deliverTo(v => count.countDown())
    count.await()

    count = new CountDownLatch(1)
    val f1 = database(select().from(collection).where((JPath("location") within Circle((30, 30), 45)) && JPath("address.city") === "A"))
    f1.deliverTo{objects => println(objects.map(v => Printer.pretty(Printer.render(v))).mkString("\n"))}
    f1.deliverTo(v => count.countDown())
    count.await()
    printObjects(f1)

    println("99999999999999999999")
    val f2 = database(select().from(collection).where(JPath("location") near (45, 45, Some(40))))
    f2.deliverTo(v => count.countDown())
    count.await()
    printObjects(f2)


    database(remove.from(collection))
    println("------------demoGeoNear------------------")
  }

  private def demoSelect{
    println("------------demoSelect------------------")
    database(insert(jObject).into(collection))

//    printObjects(database(select().from(collection)))
//    printObjects(database(select().from(collection).sortBy("address.street" >>)))
//    printObjects(database(select().from(collection).sortBy("address.city" >>).skip(1).limit(1)))
//    printObjects(database(select().from(collection).where("address.city" === "B").sortBy("address.city" >>)))
//    printObjects(database(select().from(collection).where("address.city" === "Z").sortBy("address.city" >>)))
//    printObjects(database(select().from(collection).where(JPath("address.code") === 1 && JPath("address.code") < 3)))
    printObjects(database(select().from(collection).where(evaluation("function(){return obj.address.street == 2;}"))))
    //printObjects(database(select().from(collection).where(evaluation("function() { return this.address.code > 2}"))))
//    printObjects(database(select("address.city").from(collection).sortBy("address.city" >>)))
//    println(database(count.from(collection)))
//    printObjects(database(select("address.city").from(collection).sortBy("address.city" <<)))

    database(remove.from(collection))
    println("------------demoSelect------------------")
  }
  private def demoDistinct{
    println("------------demoDistinct------------------")
    database(insert(jObject, jObject1, jObject2, jObject3).into(collection))
    printObjects(database(select().from(collection)))
//    val result = database(distinct("address.city").from(collection))

    database(remove.from(collection))
    println("------------demoDistinct------------------")
  }
  private def demoGroup{
    println("------------demoGroup------------------")
    val objects = JsonParser.parse("""{"address":{ "city":"A", "code":2, "street":"1"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "city":"A", "code":5, "street":"3"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "city":"C", "street":"1"  } }""").asInstanceOf[JObject] :: JsonParser.parse("""{"address":{ "code":3, "street":"1"  } }""").asInstanceOf[JObject] :: Nil
    database(insert(objects :_*).into(collection))

    val initial = JsonParser.parse("""{ "csum": 10.0 }""").asInstanceOf[JObject]
    val result  = database(group(initial, "function(obj,prev) { prev.csum += obj.address.code; }", "address.city").from(collection))
    
    printObject(result.map[Option[JArray]](v => Some(v)))

    database(remove.from(collection))
    println("------------demoGroup------------------")
  }
  private def demoUpdate0{
    database(insert(jobjectsWithArray: _*).into(collection))

    printObjects(database(select().from(collection)))

    database(updateMany(collection).set("foo" pull (("shape" === "square") && ("color" === "purple")).elemMatch("")))

    printObjects(database(select().from(collection)))

    database(remove.from(collection))
  }
  private def demoUpdate1{
    database(insert(jObject).into(collection))

    printObjects(database(select().from(collection)))

    database(update(collection).set(("count" inc (3))))
    database(update(collection).set(JPath(".address.street") set ("Odinzova")) .where("address.city" === "A"))

    printObjects(database(select().from(collection)))

    database(remove.from(collection))
  }
  private def demoUpdate{
    import MongoUpdateBuilder._
    println("------------demoUpdate------------------")
    insertObjects

    database(updateMany(collection).set("address" popFirst))
    printObjects(database(select().from(collection)))
    database(update(collection).set(("address.city" unset) |+| ("address.street" set ("Another Street"))).where("address.city" === "C"))
    printObjects(database(select().from(collection)))
    database(update(collection).set(jObject3).where("address.city" === "A"))
    printObjects(database(select().from(collection)))
    database(updateMany(collection).set("address.street" set ("New Street")))
    printObjects(database(select().from(collection)))

    database(remove.from(collection))
    println("------------demoUpdate------------------")
  }
  private def demoSelectOne{
    println("------------demoSelectOne------------------")
    insertObjects

    printObject(database(selectOne().from(collection).sortBy("address.city" <<)))
    printObject(database(selectOne().from(collection).sortBy("address.city" >>)))
    printObject(database(selectOne().from(collection).where("address.city" === "B").sortBy("address.city" >>)))
    printObject(database(selectOne("address.city").from(collection).sortBy("address.city" >>)))
    printObject(database(selectOne("address.city").from(collection).sortBy("address.city" <<)))

    database(remove.from(collection))
    println("------------demoSelectOne------------------")
  }

  private def printObjects(future: Future[IterableView[JObject, Iterator[JObject]]]){
    future.deliverTo{objects =>
      println("------------------------------------------------")
      println(objects.map(v => Printer.pretty(Printer.render(v))).mkString("\n"))
      println("------------------------------------------------")
    }
  }

  private def printObject[T <: JValue](future: Future[Option[T]]){
    future.deliverTo{objects =>
      println("------------------------------------------------")
      println(objects.map(v => Printer.pretty(Printer.render(v))).mkString("\n"))
      println("------------------------------------------------")
    }
  }

  private def demoRemove{
    println("------------demoRemove------------------")

    insertObjects

    database(remove.from(collection).where("address.city" === "A"))

    database(remove.from(collection))

    println("------------demoRemove------------------")
  }

  private def insertObjects{
    database(insert(jObject, jObject).into(collection))
  }
}

//db.foo.insert({"address":{ "city":"A", "code":2, "street":"1"  } })
//db.foo.insert({"address":{ "city":"C", "street":"1"  } })
//db.foo.insert({"address":{ "code":33, "street":"5"  } })
//
//db.foo.group({key: { "address.city":1 }, cond: {}, reduce: function(a,b) { b.csum += a.address.code; return b;}, initial: { csum: 0 } })


//{ group: { key: { address.city: 1.0 }, cond: {}, initial: { csum: 12.0 }, $reduce: function (obj, prev) {prev.csum += obj.address.code;} } }
//{ group: { key: { address.city: 1.0 }, cond: {}, initial: { csum: 10 }, $reduce: "function(obj,prev) { prev.csum += obj.address.code;}" } }
