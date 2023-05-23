package org.oppia.android.scripts.lint

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File

/**
 * The main entrypoint for running Protobuf lint checks.
 *
 * This script wraps the Buf (https://github.com/bufbuild/buf) utility for performing basic lint
 * checks on all protos in the repository.
 *
 * Usage:
 *   bazel run //scripts:buf -- <path_to_repo_root>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:buf -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.size == 1) { "Usage: bazel run //scripts:buf -- </path/to/repo_root>" }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    Buf(repoRoot, bazelClient).runBuf()
  }
}

/**
 * Utility for running the Buf utility as part of verifying all .proto files under [repoRoot].
 *
 * @property repoRoot the absolute [File] corresponding to the root of the inspected repository
 * @property bazelClient a [BazelClient] configured for a single repository at [repoRoot]
 */
class Buf(private val repoRoot: File, private val bazelClient: BazelClient) {
  /**
   * Performs a lint check on all proto source files in the repository, throwing an exception if any
   * have lint failures.
   */
  fun runBuf() {
    val bufConfig = File.createTempFile("buf", ".yaml").also {
      generateBufConfiguration(it)
    }.absoluteFile.normalize()
    val rootDirs = PROTO_ROOTS.map { protoRootPath ->
      File(repoRoot, protoRootPath).absoluteFile.normalize().also {
        check(it.exists() && it.isDirectory) {
          "Configured proto root isn't an existing directory: $protoRootPath."
        }
      }
    }
    rootDirs.forEach { rootDir ->
      println("Linting protos under ${rootDir.toRelativeString(repoRoot)}...")
      val (exitCode, outputLines) = bazelClient.run(
        BUF_BINARY_TARGET,
        "lint",
        rootDir.path,
        "--config",
        bufConfig.path,
        "--disable-symlinks",
        allowFailures = true
      )
      outputLines.forEach(::println)
      check(exitCode == 0) { "Buf command failed. Please fix lint issues found above manually." }
    }

    println()
    println("No proto lint issues found!")
  }

  private fun generateBufConfiguration(destFile: File) {
    // Generates a v1 Buf configuration file (https://buf.build/docs/lint/overview/#configuration).
    destFile.writeText(
      """
# Automatically generated by //scripts:buf.
version: v1
lint:
  use:${INCLUDED_LINT_CHECKS.joinToString(separator = "\n", prefix = "\n") { "    - $it" }}
  except:${EXCLUDED_LINT_CHECKS.joinToString(separator = "\n", prefix = "\n") { "    - $it" }}
"""
    )
  }

  private companion object {
    /** The individual proto directory roots in which to run Buf. */
    private val PROTO_ROOTS = listOf("model/src/main/proto", "scripts")

    /**
     * Lint checks that are to be enabled in Buf.
     *
     * 'DEFAULT' covers all checks from BASIC and MINIMAL, per https://docs.buf.build/lint-rules.
     */
    private val INCLUDED_LINT_CHECKS = listOf("DEFAULT")

    /**
     * Lint checks that are to be disabled in Buf.
     *
     * - Per https://buf.build/docs/lint/rules#package_directory_match package matching doesn't
     *   quite work correctly with how protos are set up in the codebase.
     * - Per https://buf.build/docs/lint/rules#package_version_suffix version suffixing is disabled
     *   since Android protos don't need to be versioned in how they're used.
     * - Default enum value prefixes (per https://buf.build/docs/lint/rules#default) are disabled
     *   since Java doesn't have the same namespace scoping restrictions on enums as C/C++ do, and
     *   this allows for shorter names.
     */
    private val EXCLUDED_LINT_CHECKS = listOf(
      "PACKAGE_DIRECTORY_MATCH", "PACKAGE_VERSION_SUFFIX", "ENUM_VALUE_PREFIX"
    )

    private const val BUF_BINARY_TARGET = "//scripts/third_party:buf"
  }
}
