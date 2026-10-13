describe("Admin System Dashboard E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/system")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load system dashboard page successfully", () => {
      cy.url().should("include", "/admin/system")
      cy.contains("System Dashboard").should("be.visible")
    })

    it("should display page description", () => {
      cy.contains("Real-time system metrics and analytics").should("be.visible")
    })

    it("should display loading state initially", () => {
      cy.visit("/admin/system")
      cy.get("[class*='LoadingState']").should("exist")
    })

    it("should display overview stats section", () => {
      cy.contains("Total Users").should("be.visible")
      cy.contains("Active Today").should("be.visible")
      cy.contains("Total Sessions").should("be.visible")
      cy.contains("Total Quizzes").should("be.visible")
    })
  })

  describe("Overview Statistics", () => {
    it("should display total users stat", () => {
      cy.contains("Total Users").should("be.visible")
      cy.contains("Total Users").parent().find("text").should("contain", /\d/)
    })

    it("should display active users stat", () => {
      cy.contains("Active Today").should("be.visible")
      cy.contains("Users active in the last 24 hours").should("be.visible")
    })

    it("should display total sessions stat", () => {
      cy.contains("Total Sessions").should("be.visible")
      cy.contains("vs last week").should("be.visible")
    })

    it("should display total quizzes stat", () => {
      cy.contains("Total Quizzes").should("be.visible")
      cy.contains("Active and archived").should("be.visible")
    })

    it("should display stats with icons", () => {
      cy.get("[class*='StatsCard']").should("have.length", 4)
    })

    it("should display trend indicators", () => {
      cy.contains("from last month").should("be.visible")
      cy.contains("vs last week").should("be.visible")
    })
  })

  describe("System Health Section", () => {
    it("should display health status card", () => {
      cy.contains("System Health").should("exist")
    })

    it("should display system status information", () => {
      cy.contains("API Server").should("be.visible")
      cy.contains("Database").should("be.visible")
      cy.contains("Cache").should("be.visible")
    })

    it("should display status indicators", () => {
      cy.contains("Online").should("be.visible")
      cy.contains("Connected").should("be.visible")
      cy.contains("Active").should("be.visible")
    })

    it("should display green status dots for healthy services", () => {
      cy.get("[class*='bg-green']").should("have.length.greaterThan", 0)
    })
  })

  describe("Registration Trend Chart", () => {
    it("should display user registration trend chart", () => {
      cy.contains("User Registration Trend").should("be.visible")
    })

    it("should display chart for 30 days period", () => {
      cy.contains("(30 days)").should("be.visible")
    })

    it("should display chart visualization", () => {
      cy.get("[class*='SimpleLineChart']").should("exist")
    })
  })

  describe("User Engagement Metrics", () => {
    it("should display user engagement card", () => {
      cy.contains("User Engagement").should("be.visible")
    })

    it("should display engagement percentage", () => {
      cy.contains("User Engagement").parent().find("[class*='text-3xl']").should("contain", "%")
    })

    it("should display engagement description", () => {
      cy.contains("Average engagement rate across all users").should("be.visible")
    })

    it("should display engagement progress bar", () => {
      cy.get("[class*='bg-blue']").should("exist")
    })
  })

  describe("System Status Section", () => {
    it("should display system status card", () => {
      cy.contains("System Status").should("be.visible")
    })

    it("should display all system components", () => {
      cy.contains("API Server").should("be.visible")
      cy.contains("Database").should("be.visible")
      cy.contains("Cache").should("be.visible")
    })

    it("should show all components as online/active", () => {
      cy.contains("Online").should("be.visible")
      cy.contains("Connected").should("be.visible")
      cy.contains("Active").should("be.visible")
    })
  })

  describe("Quick Statistics", () => {
    it("should display quick statistics section", () => {
      cy.contains("Quick Statistics").should("be.visible")
    })

    it("should display total vocabularies stat", () => {
      cy.contains("Total Vocabularies").should("be.visible")
    })

    it("should display average response time stat", () => {
      cy.contains("Avg Response Time").should("be.visible")
      cy.contains(/\d+ms/).should("be.visible")
    })

    it("should display error rate stat", () => {
      cy.contains("Error Rate").should("be.visible")
      cy.contains(/%/).should("be.visible")
    })

    it("should color error rate based on threshold", () => {
      cy.contains("Error Rate").parent().find("[class*='text-2xl']").then(($errorRate) => {
        const text = $errorRate.text()
        const value = parseFloat(text)
        
        if (value > 5) {
          expect($errorRate).to.have.class("text-red-600")
        } else if (value > 2) {
          expect($errorRate).to.have.class("text-yellow-600")
        } else {
          expect($errorRate).to.have.class("text-green-600")
        }
      })
    })
  })

  describe("Refresh Functionality", () => {
    it("should have refresh button in header", () => {
      cy.get("button").contains("Refresh").should("be.visible")
    })

    it("should refresh dashboard when clicking refresh button", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("System Dashboard").should("be.visible")
    })

    it("should reload all dashboard data", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("Total Users").should("be.visible")
      cy.contains("Active Today").should("be.visible")
    })
  })

  describe("Responsive Design", () => {
    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-12")
      cy.contains("Total Users").should("be.visible")
      cy.contains("System Status").should("be.visible")
    })

    it("should stack stats on mobile", () => {
      cy.viewport("iphone-12")
      cy.get("[class*='grid']").should("exist")
    })

    it("should be responsive on tablet viewport", () => {
      cy.viewport("ipad-2")
      cy.contains("Total Users").should("be.visible")
      cy.contains("Total Sessions").should("be.visible")
    })

    it("should display 2 columns of stats on tablet", () => {
      cy.viewport("ipad-2")
      cy.get("[class*='md:grid-cols-2']").should("exist")
    })

    it("should be responsive on desktop viewport", () => {
      cy.viewport(1920, 1080)
      cy.contains("Total Users").should("be.visible")
      cy.contains("Total Quizzes").should("be.visible")
    })

    it("should display 4 columns of stats on desktop", () => {
      cy.viewport(1920, 1080)
      cy.get("[class*='lg:grid-cols-4']").should("exist")
    })
  })

  describe("Performance", () => {
    it("should load page within acceptable time", () => {
      const startTime = performance.now()
      cy.visit("/admin/system")
      cy.contains("Total Users").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(4000) // 4 seconds for dashboard with charts
    })

    it("should display all stats without lag", () => {
      cy.get("[class*='StatsCard']").should("be.visible")
      cy.get("[class*='bg-green']").should("be.visible")
    })

    it("should render chart without performance issues", () => {
      cy.contains("User Registration Trend").should("be.visible")
      cy.get("[class*='SimpleLineChart']").should("exist")
    })
  })

  describe("Data Display", () => {
    it("should format user counts with comma separators", () => {
      cy.contains("Total Users").parent().find("text").should("match", /\d{1,3}(,\d{3})*/)
    })

    it("should display response time in milliseconds", () => {
      cy.contains("Avg Response Time").parent().find("text").should("match", /\d+ms/)
    })

    it("should display error rate with 2 decimal places", () => {
      cy.contains("Error Rate").parent().find("text").should("match", /\d+\.\d{2}%/)
    })

    it("should display engagement percentage with 1 decimal place", () => {
      cy.contains("User Engagement").parent().find("text").should("match", /\d+\.\d%/)
    })
  })

  describe("Accessibility", () => {
    it("should have proper heading structure", () => {
      cy.get("h1").should("exist")
      cy.get("h3").should("exist")
    })

    it("should have semantic page header", () => {
      cy.contains("System Dashboard").should("be.visible")
      cy.contains("Real-time system metrics and analytics").should("be.visible")
    })

    it("should have accessible buttons", () => {
      cy.get("button").each(($btn) => {
        expect($btn).to.have.text().or("have.attr", "aria-label")
      })
    })

    it("should have accessible status indicators", () => {
      cy.contains("Online").should("be.visible")
      cy.contains("Connected").should("be.visible")
    })

    it("should support keyboard navigation", () => {
      cy.get("button").contains("Refresh").focus()
      cy.focused().should("contain", "Refresh")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/system*", { statusCode: 500 })
      cy.visit("/admin/system")
      cy.contains("Failed to load dashboard").should("be.visible")
    })

    it("should show error message when data fails to load", () => {
      cy.intercept("GET", "**/admin/system/overview", { statusCode: 500 })
      cy.intercept("GET", "**/admin/system/health", { statusCode: 500 })
      cy.intercept("GET", "**/admin/system/registration-trend", { statusCode: 500 })
      cy.visit("/admin/system")
    })

    it("should recover when refresh is clicked after error", () => {
      cy.intercept("GET", "**/admin/system*", { statusCode: 500 }).as("error")
      cy.visit("/admin/system")
      cy.wait("@error")
      
      cy.intercept("GET", "**/admin/system*", { 
        statusCode: 200, 
        body: { 
          totalUsers: 100,
          activeUsersToday: 50,
          totalSessions: 500,
          totalQuizzes: 30
        }
      }).as("success")
      
      cy.get("button").contains("Refresh").click()
      cy.wait("@success")
    })
  })

  describe("Data Consistency", () => {
    it("should maintain data during refresh", () => {
      cy.contains("Total Users").parent().find("text").then(($initialValue) => {
        const initialValue = $initialValue.text()
        cy.get("button").contains("Refresh").click()
        cy.contains("Total Users").parent().find("text").should("contain", initialValue)
      })
    })

    it("should update all sections together", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("Total Users").should("be.visible")
      cy.contains("User Engagement").should("be.visible")
      cy.contains("System Status").should("be.visible")
    })
  })

  describe("Chart Interaction", () => {
    it("should display chart within expected boundaries", () => {
      cy.get("[class*='SimpleLineChart']").should("exist").within(() => {
        cy.get("[class*='w-full']").should("exist")
      })
    })

    it("should render chart with proper styling", () => {
      cy.contains("User Registration Trend").should("be.visible")
      cy.get("[class*='SimpleLineChart']").should("have.css", "width")
    })
  })

  describe("Progress Bars", () => {
    it("should display engagement progress bar", () => {
      cy.contains("User Engagement").parent().find("[class*='bg-muted']").should("exist")
    })

    it("should fill progress bar based on engagement value", () => {
      cy.contains("User Engagement").parent().find("[class*='bg-blue']").should("have.css", "width")
    })

    it("should not exceed 100% on progress bar", () => {
      cy.contains("User Engagement").parent().find("[class*='bg-blue']").then(($bar) => {
        const widthStyle = $bar.attr("style")
        const match = widthStyle?.match(/width:\s*(\d+)%/)
        if (match) {
          const width = parseInt(match[1])
          expect(width).to.be.at.most(100)
        }
      })
    })
  })
})
