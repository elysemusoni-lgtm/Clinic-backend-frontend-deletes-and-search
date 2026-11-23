import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [offices, setOffices] = useState([])
  const [searchText, setSearchText] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function loadOffices() {
      setLoading(true)
      setError('')

      try {
        const response = await fetch('/api/offices', { signal: controller.signal })
        if (!response.ok) {
          throw new Error('Could not load offices. Please try again.')
        }

        const data = await response.json()
        setOffices(data)
      } catch (loadError) {
        if (loadError.name !== 'AbortError') {
          setError(loadError.message || 'Could not load offices. Please try again.')
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadOffices()
    return () => controller.abort()
  }, [reload])

  const filteredOffices = offices.filter((office) => {
    const query = search.trim().toLowerCase()
    return (
      office.name?.toLowerCase().includes(query) ||
      String(office.officeNumber).includes(query)
    )
  })

  function searchOffices(event) {
    event.preventDefault()
    setSearch(searchText)
  }

  async function deleteOffice(office) {
    if (!window.confirm(`Delete office "${office.name}" (No. ${office.officeNumber})?`)) {
      return
    }

    setDeletingId(office.id)
    setError('')

    try {
      const response = await fetch(`/api/offices/${office.id}`, { method: 'DELETE' })
      if (!response.ok) {
        throw new Error('Could not delete this office. Please try again.')
      }

      setOffices((currentOffices) =>
        currentOffices.filter((currentOffice) => currentOffice.id !== office.id),
      )
    } catch (deleteError) {
      setError(deleteError.message || 'Could not delete this office. Please try again.')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <main className="page">
      <h1>The available offices</h1>

      <form className="search-form" onSubmit={searchOffices}>
        <label htmlFor="office-search">Search by office name or number:</label>
        <input
          id="office-search"
          type="search"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
        />
        <button type="submit">Search</button>
      </form>

      {error && (
        <p className="error-message" role="alert">
          {error}
          {!loading && offices.length === 0 && (
            <button type="button" onClick={() => setReload((value) => value + 1)}>
              Try again
            </button>
          )}
        </p>
      )}

      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th scope="col">Office Name</th>
              <th scope="col">Office Number</th>
              <th scope="col">Action</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="3">Loading offices...</td>
              </tr>
            ) : filteredOffices.length > 0 ? (
              filteredOffices.map((office) => (
                <tr key={office.id}>
                  <td>{office.name}</td>
                  <td>{office.officeNumber}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => deleteOffice(office)}
                      disabled={deletingId === office.id}
                    >
                      {deletingId === office.id ? 'Deleting...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3">
                  {offices.length === 0
                    ? 'No offices are available.'
                    : 'No offices match your search.'}
                </td>
                </tr>
            )}
          </tbody>
        </table>
      </div>
    </main>
  )
}

export default App
