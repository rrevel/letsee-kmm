package io.github.letsee.ui.testutils

import io.github.letsee.models.DefaultRequest
import io.github.letsee.models.CategorisedMocks
import io.github.letsee.models.Category
import io.github.letsee.models.Mock
import io.github.letsee.models.RequestStatus
import io.github.letsee.ui.RequestUIModel

object TestFixtures {
    const val LongFullUrl = "https://app.mb-stg.reference.azure.backbaseservices.com/arrangement-manager/client-api/v2/productsummary/context/arrangements"
    const val LongBaseUrlPath = "[BaseURL]/arrangement-manager/client-api/v2/productsummary/context/arrangements"

    fun requestUiModel(
        displayName: String,
        method: String = "GET",
        status: RequestStatus = RequestStatus.IDLE,
        requestId: Int = 1001,
    ): RequestUIModel {
        return RequestUIModel(
            requestId = requestId,
            displayName = displayName,
            status = status,
            categorisedMocks = emptyList(),
            request = DefaultRequest(
                headers = emptyMap(),
                requestMethod = method,
                uri = displayName,
                path = displayName,
            ),
        )
    }

    fun requestUiModelWithSpecificMocks(
        displayName: String,
        requestId: Int = 2001,
    ): RequestUIModel {
        return RequestUIModel(
            requestId = requestId,
            displayName = displayName,
            status = RequestStatus.IDLE,
            categorisedMocks = listOf(
                CategorisedMocks(
                    category = Category.SPECIFIC,
                    mocks = listOf(
                        Mock.defaultSuccess(
                            name = "ArrangementItems",
                            data = """{"status":"ok"}""".encodeToByteArray(),
                        ),
                    ),
                ),
            ),
            request = DefaultRequest(
                headers = emptyMap(),
                requestMethod = "GET",
                uri = displayName,
                path = displayName,
            ),
        )
    }
}
