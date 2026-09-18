package com.purbarun.pipeline.stage;

import java.util.List;

/// # PipelineStage\<T\> — V2 Improved (Step 2, Java 25)
///
/// Generic stage contract using bounded generics.
///
/// **Without generics (V1):** every stage accepted and returned raw or hardcoded types.
/// There was no compile-time guarantee that a `FilterStage`'s output was the correct
/// input type for `EnrichStage` — a wrong wiring was a runtime `ClassCastException`.
///
/// **With `PipelineStage<T>`:** the type parameter is enforced at compile time.
/// A `PipelineStage<Employee>` cannot be accidentally passed a `List<Department>`.
/// The compiler rejects mismatches before the code ever runs — eliminating
/// `ClassCastException` risks that existed when stages accepted untyped `Object` or `List`.
///
/// `ValidateStage` implements `PipelineStage<Employee>` immediately (it already has the
/// right signature). `FilterStage` and `EnrichStage` still carry extra configuration
/// parameters in Step 2; they will fully conform when those parameters are replaced
/// by injected `Predicate<T>` and Scoped Values in Step 5.
public interface PipelineStage<T> {

    /// Processes the input list and returns a filtered or transformed output list
    /// of the same element type.
    ///
    /// @param input the stage's input — never null
    /// @return the stage's output — never null, may be empty
    List<T> process(List<T> input);
}
