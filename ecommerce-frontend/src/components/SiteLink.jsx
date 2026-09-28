export function Link({ href, className = "", children, ...props }) {
  return (
    <a href={`#${href}`} className={className} {...props}>
      {children}
    </a>
  );
}