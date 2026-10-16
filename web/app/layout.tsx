export const metadata = {
  title: 'AGILA Hub — Admin Portal',
  description: 'The Fraternal Order of Eagles (Philippine Eagles) — Administrative Governance',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body style={{ margin: 0, padding: 0, background: '#F8FAFC' }}>
        {children}
      </body>
    </html>
  );
}
