import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class BillingManager(
    private val context: Context,
    private val purchaseUpdateListener: PurchasesUpdatedListener
) {
    private var billingClient: BillingClient? = null

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()  // Required
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing is ready
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connecting if necessary
            }
        })
    }

    fun startSubscriptionPurchase(activity: Activity, productId: String) {
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        //.setProductId(productId)
                        .setOfferToken("your_offer_token")  // Optional: Specify your specific offer token
                        .build()
                )
            )
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    fun destroy() {
        billingClient?.endConnection()
    }
}
