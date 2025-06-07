package quilt.ghgen

import com.expediagroup.graphql.client.Generated
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import kotlin.Int
import kotlin.String
import kotlin.reflect.KClass
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import quilt.ghgen.findissueid.Repository

public const val FIND_ISSUE_ID: String =
    "query FindIssueId(${'$'}repo:String!, ${'$'}issue:Int!) {\n\trepository(owner:\"QuiltMC\", name:${'$'}repo) {\n\t\tissue(number:${'$'}issue) {\n\t\t\ttitle\n\t\t\tauthor {\n\t\t\t\t__typename\n\t\t\t\t... on EnterpriseUserAccount {\n\t\t\t\t\tlogin\n\t\t\t\t\tid\n\t\t\t\t}\n\t\t\t\t... on Organization {\n\t\t\t\t\tlogin\n\t\t\t\t\tid\n\t\t\t\t}\n\t\t\t\t... on Bot {\n\t\t\t\t\tlogin\n\t\t\t\t\tid\n\t\t\t\t}\n\t\t\t\t... on Mannequin {\n\t\t\t\t\tlogin\n\t\t\t\t\tid\n\t\t\t\t}\n\t\t\t\t... on User {\n\t\t\t\t\tlogin\n\t\t\t\t\tid\n\t\t\t\t}\n\t\t\t}\n\t\t\tbody\n\t\t\tid\n\t\t}\n\t\tpullRequest(number:${'$'}issue) {\n\t\t\tid\n\t\t}\n\t}\n}"

@Generated
@Serializable
public class FindIssueId(
  override val variables: FindIssueId.Variables,
) : GraphQLClientRequest<FindIssueId.Result> {
  @Required
  override val query: String = FIND_ISSUE_ID

  @Required
  override val operationName: String = "FindIssueId"

  override fun responseType(): KClass<FindIssueId.Result> = FindIssueId.Result::class

  @Generated
  @Serializable
  public data class Variables(
    public val repo: String,
    public val issue: Int,
  )

  /**
   * The query root of GitHub's GraphQL interface.
   */
  @Generated
  @Serializable
  public data class Result(
    /**
     * Lookup a given repository by the owner and repository name.
     */
    public val repository: Repository? = null,
  )
}
