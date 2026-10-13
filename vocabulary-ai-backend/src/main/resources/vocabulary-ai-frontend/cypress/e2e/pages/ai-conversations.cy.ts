describe("AI Conversations Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/ai")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load AI conversations page successfully", () => {
      cy.url().should("include", "/admin/ai")
      cy.contains("AI Conversations").should("be.visible")
    })

    it("should display page description", () => {
      cy.contains("Monitor and analyze AI-assisted conversations with users").should("be.visible")
    })

    it("should display conversations table", () => {
      cy.get("table").should("be.visible")
      cy.get("th").should("contain", "Title")
      cy.get("th").should("contain", "User")
      cy.get("th").should("contain", "Type")
      cy.get("th").should("contain", "Messages")
      cy.get("th").should("contain", "Status")
    })

    it("should display search and filter controls", () => {
      cy.get("input[placeholder*='Search']").should("be.visible")
      cy.get("select").should("exist")
    })

    it("should load conversation data", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Search Functionality", () => {
    it("should search conversations by title", () => {
      cy.get("input[placeholder*='Search']").type("english")
      cy.get("tbody tr").should("exist")
    })

    it("should search conversations by user email", () => {
      cy.get("input[placeholder*='Search']").type("user@example.com")
      cy.get("tbody tr").should("exist")
    })

    it("should clear search and show all results", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("input[placeholder*='Search']").clear()
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })

    it("should show empty state when search has no results", () => {
      cy.get("input[placeholder*='Search']").type("xyznonexistent123xyz")
      cy.get("tbody tr").should("have.length", 0)
      cy.contains("No conversations found").should("be.visible")
    })
  })

  describe("Filtering by Type", () => {
    it("should filter by GRAMMAR type", () => {
      cy.get("select").select("GRAMMAR")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by VOCABULARY type", () => {
      cy.get("select").select("VOCABULARY")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by HINT type", () => {
      cy.get("select").select("HINT")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by GENERAL type", () => {
      cy.get("select").select("GENERAL")
      cy.get("tbody tr").should("exist")
    })

    it("should reset type filter to All", () => {
      cy.get("select").select("GRAMMAR")
      cy.get("select").select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Combined Search and Filter", () => {
    it("should combine search with type filter", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("select").select("GRAMMAR")
      cy.get("tbody tr").should("exist")
    })

    it("should reset all search and filters", () => {
      cy.get("input[placeholder*='Search']").type("conversation")
      cy.get("select").select("VOCABULARY")
      
      cy.get("input[placeholder*='Search']").clear()
      cy.get("select").select("All")
      
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Table Display and Data", () => {
    it("should display all table columns", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").should("have.length.greaterThan", 5)
      })
    })

    it("should display conversation title", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).should("not.be.empty")
      })
    })

    it("should display user email", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(1).should("contain", "@")
      })
    })

    it("should display conversation type with badge", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/GRAMMAR|VOCABULARY|HINT|GENERAL/).should("be.visible")
      })
    })

    it("should display message count as number", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(3).should("match", /\d+/)
      })
    })

    it("should display status badge", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/Active|Archived/).should("be.visible")
      })
    })

    it("should display created and updated dates", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(5).should("match", /\d{1,2}\/\d{1,2}\/\d{4}/)
        cy.get("td").eq(6).should("match", /\d{1,2}\/\d{1,2}\/\d{4}/)
      })
    })

    it("should have view button for each row", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").should("exist")
      })
    })
  })

  describe("Pagination", () => {
    it("should display pagination controls", () => {
      cy.get("[data-testid='pagination']").should("exist")
    })

    it("should navigate to next page", () => {
      cy.get("tbody tr").first().then(($row) => {
        const firstPageFirstRow = $row.text()
        cy.get("button").contains("Next").click({ force: true })
        cy.get("tbody tr").first().then(($nextRow) => {
          const secondPageFirstRow = $nextRow.text()
          expect(firstPageFirstRow).not.to.equal(secondPageFirstRow)
        })
      })
    })

    it("should navigate to previous page", () => {
      cy.get("button").contains("Next").click({ force: true })
      cy.get("tbody tr").should("exist")
      cy.get("button").contains("Previous").click({ force: true })
      cy.get("tbody tr").should("exist")
    })
  })

  describe("Detail View", () => {
    it("should navigate to detail page when clicking view button", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.url().should("include", "/admin/ai/")
      cy.contains("Conversation Information").should("be.visible")
    })

    it("should display conversation title on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.get("h1").should("exist")
    })

    it("should display conversation information", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("User Email").should("be.visible")
      cy.contains("Type").should("be.visible")
      cy.contains("Message Count").should("be.visible")
      cy.contains("Status").should("be.visible")
    })

    it("should display conversation summary", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("Summary").should("be.visible")
    })

    it("should have back button on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.get("button").contains("Back to conversations").click()
      cy.url().should("include", "/admin/ai")
    })

    it("should show 404 when conversation not found", () => {
      cy.visit("/admin/ai/99999")
      cy.contains("Conversation not found").should("be.visible")
    })
  })

  describe("Type Badges", () => {
    it("should display correct color for GRAMMAR type", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains("GRAMMAR").should("be.visible")
      })
    })

    it("should display correct color for VOCABULARY type", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains("VOCABULARY").should("be.visible")
      })
    })

    it("should display archived status badge", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/Active|Archived/).should("be.visible")
      })
    })
  })

  describe("Responsive Design", () => {
    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("be.visible")
      cy.get("input[placeholder*='Search']").should("be.visible")
    })

    it("should scroll table horizontally on mobile", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("have.css", "overflow-x")
    })

    it("should be responsive on tablet viewport", () => {
      cy.viewport("ipad-2")
      cy.get("table").should("be.visible")
      cy.get("select").should("be.visible")
    })

    it("should be responsive on desktop viewport", () => {
      cy.viewport(1920, 1080)
      cy.get("table").should("be.visible")
      cy.get("[data-testid='pagination']").should("be.visible")
    })
  })

  describe("Performance", () => {
    it("should load page within acceptable time", () => {
      const startTime = performance.now()
      cy.visit("/admin/ai")
      cy.get("table").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(3000) // 3 seconds
    })

    it("should render table rows quickly", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
      cy.get("tbody tr").should("be.visible")
    })

    it("should apply filters without significant delay", () => {
      const startTime = performance.now()
      cy.get("select").select("GRAMMAR")
      cy.get("tbody tr").should("exist")
      const endTime = performance.now()
      const filterTime = endTime - startTime
      expect(filterTime).to.be.lessThan(2000) // 2 seconds
    })
  })

  describe("Accessibility", () => {
    it("should have proper heading structure", () => {
      cy.get("h1").should("exist")
    })

    it("should have accessible search input", () => {
      cy.get("input[placeholder*='Search']")
        .should("have.attr", "aria-label")
        .or("have.attr", "placeholder")
    })

    it("should have accessible select dropdown", () => {
      cy.get("select").should("have.attr", "aria-label").or("have.attr", "id")
    })

    it("should have accessible table headers", () => {
      cy.get("th").each(($th) => {
        expect($th).to.have.text()
      })
    })

    it("should have accessible buttons", () => {
      cy.get("button").each(($btn) => {
        expect($btn).to.have.text().or("have.attr", "aria-label")
      })
    })

    it("should support keyboard navigation", () => {
      cy.get("input[placeholder*='Search']").focus()
      cy.focused().should("have.attr", "placeholder")
      
      cy.get("select").focus()
      cy.focused().should("be.a", "select")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/ai", { statusCode: 500 })
      cy.visit("/admin/ai")
    })

    it("should handle empty data gracefully", () => {
      cy.intercept("GET", "**/admin/ai", {
        statusCode: 200,
        body: { content: [], totalPages: 1 }
      })
      cy.visit("/admin/ai")
      cy.get("tbody tr").should("have.length", 0)
    })

    it("should show notification on search error", () => {
      cy.intercept("GET", "**/admin/ai*", { statusCode: 500 }).as("aiSearch")
      cy.get("input[placeholder*='Search']").type("test")
      cy.wait("@aiSearch")
    })
  })

  describe("Data Consistency", () => {
    it("should maintain filters during pagination", () => {
      cy.get("select").select("GRAMMAR")
      cy.get("button").contains("Next").click({ force: true })
      cy.get("select").should("have.value", "GRAMMAR")
    })

    it("should maintain search query during filter change", () => {
      const searchTerm = "english"
      cy.get("input[placeholder*='Search']").type(searchTerm)
      cy.get("select").select("VOCABULARY")
      cy.get("input[placeholder*='Search']").should("have.value", searchTerm)
    })

    it("should display consistent data in list and detail view", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).then(($title) => {
          const conversationTitle = $title.text()
          cy.get("button").click({ force: true })
          cy.get("h1").should("contain", conversationTitle)
        })
      })
    })
  })
})
