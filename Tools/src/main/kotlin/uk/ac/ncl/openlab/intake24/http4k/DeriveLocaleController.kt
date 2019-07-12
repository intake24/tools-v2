package uk.ac.ncl.openlab.intake24.http4k

import com.google.inject.Inject
import org.http4k.core.MultipartFormBody
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import uk.ac.ncl.intake24.serialization.StringCodec
import uk.ac.ncl.openlab.intake24.tools.CsvParseException
import uk.ac.ncl.openlab.intake24.tools.DeriveLocaleCsvParser
import uk.ac.ncl.openlab.intake24.tools.DeriveLocaleParseException
import uk.ac.ncl.openlab.intake24.tools.DeriveLocaleService

class DeriveLocaleController @Inject constructor(private val service: DeriveLocaleService,
                                                 private val errorUtils: ErrorUtils) {

    private val logger = LoggerFactory.getLogger(DeriveLocaleController::class.java)

    fun deriveLocale(user: Intake24User, request: Request): Response {

        val form = MultipartFormBody.from(request)
        val file = form.file("file")
        val sourceLocale = form.field("sourceLocale")
        val destLocale = form.field("destLocale")
        val foodCompositionTable = form.field("foodCompositionTable")

        if (file == null || sourceLocale == null || destLocale == null || foodCompositionTable == null)
            return errorUtils.errorResponse(Status.BAD_REQUEST, "Missing required field")
        else {

            try {
                val actions = DeriveLocaleCsvParser.parseTable(file.content, foodCompositionTable)
                service.deriveLocale(sourceLocale, destLocale, actions)
                return Response(Status.OK)
            } catch (e: DeriveLocaleParseException) {
                return errorUtils.errorResponse(Status.BAD_REQUEST, e)
            }
        }

    }

}