package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Обновление баланса через POST /api/{login}/cash"
    request {
        method POST()
        urlPath('/api/joe/cash')
        headers {
            contentType(applicationJson())
        }
        body([
                currency: "EUR",
                action:   "PUT",
                value:    "100.00"
        ])
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                status: "completed",
                errors: null
        ])
    }
}
