package ai.arena.hisabdar.modern1405.util;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Recommendation 1: On-Device Retrieval-Augmented Generation (Local Mobile RAG).
 * Stores local text chunks (from business ledger notes) and simulated high-dimensional binary embeddings.
 * Performs lightning-fast cosine similarity vector search on-device, keeping 100% of the merchant data confidential.
 */
public class LocalRagVectorDatabase {

    private static final String TAG = "LocalRagVectorDatabase";

    public static class VectorRecord {
        public final long id;
        public final String textContent;
        public final float[] embedding; // e.g. 384 dimensions for Mobile SBERT/Gecko

        public VectorRecord(long id, String textContent, float[] embedding) {
            this.id = id;
            this.textContent = textContent;
            this.embedding = embedding;
        }
    }

    private final List<VectorRecord> localStore;

    public LocalRagVectorDatabase() {
        this.localStore = new ArrayList<>();
        populateMockBazaarEmbeddings();
    }

    private void populateMockBazaarEmbeddings() {
        // Populates some typical ledger chunks with mock embeddings
        localStore.add(new VectorRecord(1, "مشتری رضا اصغری همیشه چک‌های خود را سر تاریخ ۳۰ روزه پاس می‌کند. امین آهن بازار صنف تهران.", new float[]{0.15f, -0.45f, 0.78f}));
        localStore.add(new VectorRecord(2, "دفتر نسیه مرتضی رحیمی بابت خرید میلگرد البرز فاکتور ۱۲۲ بدهی ۵۰ میلیونی دارد. دیرکرد ۶۵ روزه دارد و بدقول است.", new float[]{-0.22f, 0.65f, -0.12f}));
        localStore.add(new VectorRecord(3, "کالای نبشی ۱۲ در قفسه انبار شماره ۴ انباشته شده است. ۳ ماه است بدون چرخش مالی خاک می‌خورد.", new float[]{0.85f, 0.05f, 0.44f}));
    }

    /**
     * Calculates cosine similarity between two high-dimensional float vectors on-device.
     */
    public static float calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) return 0f;
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    /**
     * Performs a local semantic search over the local ledger knowledge base.
     * Keeps merchant data 100% private.
     */
    public List<VectorRecord> searchLedgerSemantically(float[] queryVector, float threshold) {
        List<VectorRecord> matches = new ArrayList<>();
        Log.i(TAG, "Running On-Device Vector similarity search (HNSW alternative)...");
        
        for (VectorRecord record : localStore) {
            float sim = calculateCosineSimilarity(queryVector, record.embedding);
            if (sim >= threshold) {
                matches.add(record);
                Log.d(TAG, "Local RAG Match found (Sim: " + sim + "): " + record.textContent);
            }
        }
        return matches;
    }
}
