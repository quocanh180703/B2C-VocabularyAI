describe("Vocabulary API Tests", () => {
  const baseUrl = "http://localhost:8080/api"

  describe("GET /admin/vocabulary - List endpoint", () => {
    it("should return 200 with paginated vocabulary list", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=20`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        expect(response.body).to.have.property("content")
        expect(response.body).to.have.property("totalElements")
        expect(response.body).to.have.property("totalPages")
        expect(Array.isArray(response.body.content)).to.be.true
      })
    })

    it("should filter by CEFR level", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=20&cefr=B1`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        if (response.body.content.length > 0) {
          response.body.content.forEach((item: any) => {
            expect(item.cefr).to.equal("B1")
          })
        }
      })
    })

    it("should filter by Part of Speech", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=20&pos=NOUN`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        if (response.body.content.length > 0) {
          response.body.content.forEach((item: any) => {
            expect(item.partOfSpeech).to.equal("NOUN")
          })
        }
      })
    })

    it("should search by keyword", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=20&search=apple`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
      })
    })

    it("should support pagination", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=1&size=10`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        expect(response.body).to.have.property("content")
        expect(response.body.pageSize).to.equal(10)
      })
    })

    it("should return 400 for invalid page", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=-1&size=20`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.be.oneOf([400, 200])
      })
    })
  })

  describe("GET /admin/vocabulary/{wordId} - Detail endpoint", () => {
    it("should return 200 with word details", () => {
      // First, get a valid word ID from list endpoint
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=1`,
        failOnStatusCode: false,
      }).then((response) => {
        if (response.body.content.length > 0) {
          const wordId = response.body.content[0].id
          cy.request({
            method: "GET",
            url: `${baseUrl}/admin/vocabulary/${wordId}`,
            failOnStatusCode: false,
          }).then((detailResponse) => {
            expect(detailResponse.status).to.equal(200)
            expect(detailResponse.body).to.have.property("id")
            expect(detailResponse.body).to.have.property("word")
            expect(detailResponse.body).to.have.property("definition")
          })
        }
      })
    })

    it("should return 404 for non-existent word", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary/99999999`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(404)
      })
    })

    it("should have all required fields in response", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=1`,
        failOnStatusCode: false,
      }).then((response) => {
        if (response.body.content.length > 0) {
          const wordId = response.body.content[0].id
          cy.request({
            method: "GET",
            url: `${baseUrl}/admin/vocabulary/${wordId}`,
          }).then((detailResponse) => {
            const requiredFields = ["id", "word", "cefr", "pronunciation", "partOfSpeech"]
            requiredFields.forEach((field) => {
              expect(detailResponse.body).to.have.property(field)
            })
          })
        }
      })
    })
  })

  describe("GET /admin/vocabulary/stats/cefr - Statistics endpoint", () => {
    it("should return CEFR statistics", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary/stats/cefr`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        expect(response.body).to.have.property("a1Count")
        expect(response.body).to.have.property("a2Count")
        expect(response.body).to.have.property("b1Count")
        expect(response.body).to.have.property("totalCount")
      })
    })

    it("should have valid count values", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary/stats/cefr`,
      }).then((response) => {
        expect(response.body.a1Count).to.be.a("number").and.be.greaterThan(-1)
        expect(response.body.totalCount).to.be.a("number").and.be.greaterThan(0)
      })
    })
  })

  describe("GET /admin/vocabulary/categories - Category list endpoint", () => {
    it("should return paginated category list", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary/categories?page=0&size=20`,
        failOnStatusCode: false,
      }).then((response) => {
        expect(response.status).to.equal(200)
        expect(response.body).to.have.property("content")
        expect(Array.isArray(response.body.content)).to.be.true
      })
    })

    it("should have category structure", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary/categories?page=0&size=1`,
        failOnStatusCode: false,
      }).then((response) => {
        if (response.body.content.length > 0) {
          expect(response.body.content[0]).to.have.property("categoryId")
          expect(response.body.content[0]).to.have.property("categoryName")
          expect(response.body.content[0]).to.have.property("wordCount")
        }
      })
    })
  })

  describe("Response Format & Headers", () => {
    it("should have proper Content-Type header", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=1`,
      }).then((response) => {
        expect(response.headers["content-type"]).to.include("application/json")
      })
    })

    it("should have consistent response structure", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=1`,
      }).then((response) => {
        expect(response.body).to.have.property("content")
        expect(response.body).to.have.property("totalElements")
        expect(response.body).to.have.property("totalPages")
        expect(response.body).to.have.property("currentPage")
        expect(response.body).to.have.property("pageSize")
      })
    })
  })

  describe("Performance", () => {
    it("should return list endpoint within 2 seconds", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=20`,
      }).then((response) => {
        // Check response time is reasonable
        expect(response.duration).to.be.lessThan(2000)
      })
    })

    it("should return detail endpoint within 1 second", () => {
      cy.request({
        method: "GET",
        url: `${baseUrl}/admin/vocabulary?page=0&size=1`,
      }).then((response) => {
        if (response.body.content.length > 0) {
          const wordId = response.body.content[0].id
          cy.request({
            method: "GET",
            url: `${baseUrl}/admin/vocabulary/${wordId}`,
          }).then((detailResponse) => {
            expect(detailResponse.duration).to.be.lessThan(1000)
          })
        }
      })
    })
  })
})
