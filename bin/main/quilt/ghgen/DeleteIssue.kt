package quilt.ghgen

import com.expediagroup.graphql.client.Generated
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import kotlin.String
import kotlin.reflect.KClass
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import quilt.ghgen.deleteissue.DeleteIssuePayload

public const val DELETE_ISSUE: String =
    "mutation DeleteIssue(${'$'}id:ID!) {\n\tdeleteIssue(input: {\n\t\tissueId:${'$'}id\n\t}) {\n\t\tclientMutationId\n\t}\n}"

@Generated
@Serializable
public class DeleteIssue(
  override val variables: DeleteIssue.Variables,
) : GraphQLClientRequest<DeleteIssue.Result> {
  @Required
  override val query: String = DELETE_ISSUE

  @Required
  override val operationName: String = "DeleteIssue"

  override fun responseType(): KClass<DeleteIssue.Result> = DeleteIssue.Result::class

  @Generated
  @Serializable
  public data class Variables(
    public val id: ID,
  )

  /**
   * The root query for implementing GraphQL mutations.
   */
  @Generated
  @Serializable
  public data class Result(
    /**
     * Deletes an Issue object.
     */
    public val deleteIssue: DeleteIssuePayload? = null,
  )
}
