export function LoadingState({ label = "Loading" }) {
  return <div className="page-feedback" role="status"><span className="loader" />{label}</div>;
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="page-feedback error-feedback" role="alert">
      <p>{message}</p>
      {onRetry && <button className="button button-outline" onClick={onRetry}>Try again</button>}
    </div>
  );
}