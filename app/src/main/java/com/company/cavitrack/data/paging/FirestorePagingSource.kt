package com.company.cavitrack.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestorePagingSource<T : Any>(
    private val baseQuery: Query,
    private val mapper: (DocumentSnapshot) -> T?
) : PagingSource<DocumentSnapshot, T>() {
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, T> {
        return try {
            var currentPage = baseQuery.limit(params.loadSize.toLong())
            params.key?.let {
                currentPage = currentPage.startAfter(it)
            }
            val snapshot = currentPage.get().await()
            val nextKey = if (snapshot.size() < params.loadSize) null else snapshot.documents.lastOrNull()
            
            val data = snapshot.documents.mapNotNull { mapper(it) }
            
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<DocumentSnapshot, T>): DocumentSnapshot? = null
}
