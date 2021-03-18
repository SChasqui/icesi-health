package com.example

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.text.get
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration


object Files : Table("files") {
    val id = integer("id")
    val name = varchar("name", length = 30)
    val path = varchar("path",length=50)
    val type = varchar("type",length=30)
}
data class MyFiles(val id: Int, val name: String, val path: String, val type: String)

fun initDB() {
    val url = "jdbc:mysql://localhost:3306/icesihealth"
    val driver = "com.mysql.cj.jdbc.Driver"
    Database.connect(url, driver, "root","password")
}

fun getFilesData(): String {
    var json: String = ""
    transaction {
        val res = Files.selectAll().orderBy(Files.id, false)
        val c = ArrayList<MyFiles>()
        for (f in res) {
            c.add(MyFiles(id = f[Files.id], name = f[Files.name], path = f[Files.path], type = f[Files.type]))
        }
        json = Gson().toJson(c);
    }
    return json
}

fun saveFileInDB(pName: String, pType: String) {
    transaction {
        Files.insert {
            it[name] = pName
            it[path] = "/test/"
            it[type] = pType
        }
    }
}
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    install(CORS)
    {
        method(HttpMethod.Options)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        // host("my-host:80")
        // host("my-host", subDomains = listOf("www"))
        // host("my-host", schemes = listOf("http", "https"))
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAge = Duration.ofDays(1)
    }
    initDB()

    routing {
        get("/") {
            call.respondText(getFilesData(), ContentType.Text.Plain)
        }

        get("/avstorage") {
            val file = java.io.File("/boot/efi").usableSpace/1024/1024/1024
            call.respondText(file.toString() + " GB", ContentType.Text.Plain)
        }

        get("/{name}") {
            // get filename from request url
            val filename = call.parameters["name"]!!
            // construct reference to file
            // ideally this would use a different filename
            val file = java.io.File("./uploads/$filename")
            //Force browser to download insteda of prompt
            call.response.header("Content-Disposition", "attachment; filename=\"${file.name}\"")
            if(file.exists()) {
                call.respondFile(file)
            }
            else call.respond(HttpStatusCode.NotFound)
        }


        post("/uploads"){
            // retrieve all multipart data (suspending)
            val multipart = call.receiveMultipart()
            var nameResponse: String = ""
            var typeResponse: String = ""
            multipart.forEachPart { part ->
                // if part is a file (could be form item)
                if(part is PartData.FileItem) {
                    // retrieve file name of upload
                    val name = part.originalFileName!!

                    val file = java.io.File("./uploads/$name")
                    nameResponse = name
                    typeResponse = name.substringAfterLast('.', "")

                    // use InputStream from part to save file
                    part.streamProvider().use { its ->
                        // copy the stream to the file with buffering
                        file.outputStream().buffered().use {
                            // note that this is blocking
                            its.copyTo(it)
                        }
                    }
                }
                // make sure to dispose of the part after use to prevent leaks
                part.dispose()
            }
            saveFileInDB(nameResponse, typeResponse)
            call.respondText("Archivo cargado exitosamente", ContentType.Text.Plain)
        }


    }
}



