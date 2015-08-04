package org.infinispan.query.remote.client;

import org.infinispan.protostream.MessageMarshaller;
import org.infinispan.protostream.WrappedMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author anistor@redhat.com
 * @since 6.0
 */
public final class QueryResponse {

   private int numResults;

   private int projectionSize;

   private List<WrappedMessage> results;

   private long totalResults;

   public int getNumResults() {
      return numResults;
   }

   public void setNumResults(int numResults) {
      this.numResults = numResults;
   }

   public int getProjectionSize() {
      return projectionSize;
   }

   public void setProjectionSize(int projectionSize) {
      this.projectionSize = projectionSize;
   }

   public List<WrappedMessage> getResults() {
      return results;
   }

   public void setResults(List<WrappedMessage> results) {
      this.results = results;
   }

   public long getTotalResults() {
      return totalResults;
   }

   public void setTotalResults(long totalResults) {
      this.totalResults = totalResults;
   }

   public static final class Marshaller implements MessageMarshaller<QueryResponse> {

      @Override
      public QueryResponse readFrom(ProtoStreamReader reader) throws IOException {
         QueryResponse queryResponse = new QueryResponse();
         queryResponse.setNumResults(reader.readInt("numResults"));
         queryResponse.setProjectionSize(reader.readInt("projectionSize"));
         queryResponse.setResults(reader.readCollection("results", new ArrayList<WrappedMessage>(), WrappedMessage.class));
         queryResponse.setTotalResults(reader.readLong("totalResults"));
         return queryResponse;
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, QueryResponse queryResponse) throws IOException {
         writer.writeInt("numResults", queryResponse.getNumResults());
         writer.writeInt("projectionSize", queryResponse.getProjectionSize());
         writer.writeCollection("results", queryResponse.getResults(), WrappedMessage.class);
         writer.writeLong("totalResults", queryResponse.getTotalResults());
      }

      @Override
      public Class<QueryResponse> getJavaClass() {
         return QueryResponse.class;
      }

      @Override
      public String getTypeName() {
         return "org.infinispan.query.remote.client.QueryResponse";
      }
   }
}
