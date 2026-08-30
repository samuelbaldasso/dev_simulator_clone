package dev.devsimulator.challenge.adapter.out.memory;

import dev.devsimulator.challenge.domain.Challenge;
import dev.devsimulator.challenge.domain.Difficulty;
import java.util.List;

final class ChallengeSeeder {

  private static final String JS = "javascript";

  private ChallengeSeeder() {}

  static List<Challenge> seed() {
    return List.of(
        // BEGINNER
        new Challenge(1L, "Fix cart total calculation", Difficulty.BEGINNER, 60,
            "Correct tax calculation in CartService.", JS,
            """
            function calculateTotal(subtotal) {
              var tax = subtotal * 0.08;
              return subtotal;
            }
            """,
            """
            assertEqual(calculateTotal(100), 108, "adds 8% tax to subtotal");
            assertEqual(calculateTotal(0), 0, "handles zero subtotal");
            """),
        new Challenge(2L, "Rename misleading variable", Difficulty.BEGINNER, 50,
            "Rename `d` to `discountRate` and use it correctly in applyDiscount.", JS,
            """
            function applyDiscount(price, d) {
              return price - price * d;
            }
            """,
            """
            assertEqual(applyDiscount(200, 0.1), 180, "applies 10% discount");
            assertEqual(applyDiscount(50, 0), 50, "no discount when rate is zero");
            """),
        new Challenge(3L, "Add null check on user email", Difficulty.BEGINNER, 70,
            "Prevent an error when registering a user without an email.", JS,
            """
            function normalizeEmail(email) {
              return email.trim().toLowerCase();
            }
            """,
            """
            assertEqual(normalizeEmail("  A@B.com "), "a@b.com", "trims and lowercases a valid email");
            assertEqual(normalizeEmail(null), null, "returns null instead of throwing when email is missing");
            """),
        new Challenge(4L, "Fix off-by-one in pagination", Difficulty.BEGINNER, 80,
            "Correct the loop bound so the last page includes its final item.", JS,
            """
            function lastPageItems(items, pageSize) {
              var start = items.length - (items.length % pageSize || pageSize);
              var result = [];
              for (var i = start; i < items.length - 1; i++) {
                result.push(items[i]);
              }
              return result;
            }
            """,
            """
            assertEqual(lastPageItems([1,2,3,4,5], 2), [4,5], "last page of 5 items with page size 2 keeps both items");
            assertEqual(lastPageItems([1,2,3], 3), [1,2,3], "single full page returns every item");
            """),
        new Challenge(5L, "Format currency output", Difficulty.BEGINNER, 55,
            "Ensure prices always render with 2 decimal places.", JS,
            """
            function formatPrice(value) {
              return String(value);
            }
            """,
            """
            assertEqual(formatPrice(9), "9.00", "whole numbers get two decimal places");
            assertEqual(formatPrice(9.5), "9.50", "one decimal place is padded to two");
            """),

        // JUNIOR
        new Challenge(6L, "Implement signup form validation", Difficulty.JUNIOR, 130,
            "Validate email, password strength and required fields on signup.", JS,
            """
            function validateSignup(email, password) {
              return { valid: true, errors: [] };
            }
            """,
            """
            var weak = validateSignup("user@test.com", "123");
            assertEqual(weak.valid, false, "rejects a password shorter than 8 characters");
            var badEmail = validateSignup("not-an-email", "goodPass123");
            assertEqual(badEmail.valid, false, "rejects an email without an @");
            var ok = validateSignup("user@test.com", "goodPass123");
            assertEqual(ok.valid, true, "accepts a valid email and strong password");
            """),
        new Challenge(7L, "Add unit tests for OrderService", Difficulty.JUNIOR, 150,
            "Implement calculateOrderTotal covering the happy path and an empty cart.", JS,
            """
            function calculateOrderTotal(items) {
              return items.reduce((sum, item) => sum + item.price * item.quantity, 0);
            }
            """,
            """
            assertEqual(calculateOrderTotal([{price: 10, quantity: 2}, {price: 5, quantity: 1}]), 25, "sums price times quantity across items");
            assertEqual(calculateOrderTotal([]), 0, "empty cart totals to zero");
            """),
        new Challenge(8L, "Fix broken sort order in product listing", Difficulty.JUNIOR, 120,
            "Sorting by price ascending currently returns items out of order.", JS,
            """
            function sortByPriceAscending(products) {
              return [...products].sort((a, b) => b.price - a.price);
            }
            """,
            """
            var sorted = sortByPriceAscending([{name: "b", price: 30}, {name: "a", price: 10}, {name: "c", price: 20}]);
            assertEqual(sorted.map(p => p.name), ["a", "c", "b"], "orders products from cheapest to most expensive");
            """),
        new Challenge(9L, "Handle duplicate SKU on inventory update", Difficulty.JUNIOR, 160,
            "Reject inventory updates that reuse an existing SKU.", JS,
            """
            function addInventoryItem(inventory, item) {
              inventory.push(item);
              return { ok: true };
            }
            """,
            """
            var inventory = [{sku: "A1", qty: 5}];
            var result = addInventoryItem(inventory, {sku: "A1", qty: 3});
            assertEqual(result.ok, false, "rejects an item whose SKU already exists in inventory");
            var result2 = addInventoryItem(inventory, {sku: "B2", qty: 3});
            assertEqual(result2.ok, true, "accepts an item with a new SKU");
            """),
        new Challenge(10L, "Extract magic numbers into constants", Difficulty.JUNIOR, 110,
            "Implement free shipping using a named threshold instead of a hardcoded number.", JS,
            """
            function qualifiesForFreeShipping(orderTotal) {
              return orderTotal > 999;
            }
            """,
            """
            assertEqual(qualifiesForFreeShipping(50), true, "free shipping threshold should be $50, not $999");
            assertEqual(qualifiesForFreeShipping(49.99), false, "orders just under $50 do not qualify");
            """),

        // MID
        new Challenge(11L, "Refactor discounts with Strategy pattern", Difficulty.MID, 220,
            "Implement getDiscountRate for VIP, SEASONAL and NONE strategies.", JS,
            """
            function getDiscountRate(strategy) {
              return 0;
            }
            """,
            """
            assertEqual(getDiscountRate("VIP"), 0.2, "VIP strategy gives a 20% discount");
            assertEqual(getDiscountRate("SEASONAL"), 0.1, "SEASONAL strategy gives a 10% discount");
            assertEqual(getDiscountRate("NONE"), 0, "NONE strategy gives no discount");
            """),
        new Challenge(12L, "Fix race condition in inventory reservation", Difficulty.MID, 260,
            "Reserve stock without allowing it to go negative under concurrent requests.", JS,
            """
            function reserveStock(state, quantity) {
              state.available -= quantity;
              return { ok: true };
            }
            """,
            """
            var state = { available: 2 };
            var first = reserveStock(state, 2);
            assertEqual(first.ok, true, "reserves stock when enough is available");
            var second = reserveStock(state, 1);
            assertEqual(second.ok, false, "rejects a reservation that would oversell the item");
            assertEqual(state.available, 0, "available stock never goes negative");
            """),
        new Challenge(13L, "Add caching layer for product catalog reads", Difficulty.MID, 210,
            "Implement a read-through cache so repeated lookups don't hit the loader twice.", JS,
            """
            function createCatalogCache(loader) {
              return { get: (id) => loader(id) };
            }
            """,
            """
            var calls = 0;
            var cache = createCatalogCache((id) => { calls++; return { id: id }; });
            cache.get(1);
            cache.get(1);
            assertEqual(calls, 1, "second read for the same id is served from cache, not the loader");
            """),
        new Challenge(14L, "Implement idempotent payment webhook handler", Difficulty.MID, 250,
            "Ensure duplicate webhook deliveries don't double-charge an order.", JS,
            """
            function handleWebhook(processedIds, event) {
              processedIds.add(event.id);
              return { charged: true };
            }
            """,
            """
            var processed = new Set();
            var first = handleWebhook(processed, { id: "evt_1" });
            assertEqual(first.charged, true, "first delivery of an event charges the order");
            var second = handleWebhook(processed, { id: "evt_1" });
            assertEqual(second.charged, false, "a duplicate delivery of the same event id must not charge again");
            """),
        new Challenge(15L, "Migrate email sending to async queue", Difficulty.MID, 230,
            "Enqueue the email job instead of sending it synchronously.", JS,
            """
            function sendWelcomeEmail(queue, user) {
              return { sentSynchronously: true };
            }
            """,
            """
            var queue = [];
            var result = sendWelcomeEmail(queue, { email: "a@b.com" });
            assertEqual(queue.length, 1, "enqueues exactly one job instead of sending inline");
            assertEqual(result.sentSynchronously, undefined, "no longer reports a synchronous send");
            """),

        // SENIOR (design-heavy; some remain non-runnable)
        new Challenge(16L, "Design rate limiting for the public API", Difficulty.SENIOR, 340,
            "Protect public endpoints from abuse with a token-bucket rate limiter.", null, null, null),
        new Challenge(17L, "Fix N+1 query in order history endpoint", Difficulty.SENIOR, 320,
            "Order history loads each item's product in a separate query.", null, null, null),
        new Challenge(18L, "Add circuit breaker for payment gateway", Difficulty.SENIOR, 360,
            "Prevent cascading failures when the external payment gateway degrades.", null, null, null),
        new Challenge(19L, "Redesign checkout to support partial refunds", Difficulty.SENIOR, 380,
            "Current checkout model can't represent a partially refunded order.", null, null, null),
        new Challenge(20L, "Optimize slow report generation query", Difficulty.SENIOR, 350,
            "The monthly sales report query has a P95 above 5 seconds.", null, null, null),

        // STAFF (design-only, no runnable code)
        new Challenge(21L, "Design multi-tenant data isolation strategy", Difficulty.STAFF, 500,
            "Define how tenant data is isolated as the platform moves to multi-tenancy.", null, null, null),
        new Challenge(22L, "Define event-driven architecture for order lifecycle", Difficulty.STAFF, 520,
            "Propose an event model to decouple order, inventory and shipping services.", null, null, null),
        new Challenge(23L, "Plan monolith-to-service checkout extraction", Difficulty.STAFF, 550,
            "Lead the extraction of checkout into its own deployable service.", null, null, null),
        new Challenge(24L, "Define SLOs and error budget for payments API", Difficulty.STAFF, 480,
            "Establish measurable reliability targets for the payments API.", null, null, null),
        new Challenge(25L, "Design zero-downtime migration for orders table", Difficulty.STAFF, 560,
            "Plan a schema migration for the orders table with no service interruption.", null, null, null));
  }
}
